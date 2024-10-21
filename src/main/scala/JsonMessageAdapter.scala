package bare.chatserver

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.SourceQueueWithComplete
import akka.stream.QueueOfferResult
import play.api.libs.json._
import scala.concurrent.ExecutionContext

object JsonMessageAdapter extends Logging {
  def behavior(queue: SourceQueueWithComplete[ChatRoomActor.Response])(implicit
      ec: ExecutionContext
  ): Behavior[ChatRoomActor.Response] =
    Behaviors.receiveMessage { msg =>
      queue.offer(msg).foreach {
        case QueueOfferResult.Enqueued => ()
        case QueueOfferResult.Dropped  => logger.warn(s"Dropped message: $msg")
        case QueueOfferResult.Failure(ex) =>
          logger.error(s"Enqueue failed: $ex", ex)
        case QueueOfferResult.QueueClosed =>
          logger.info("Queue was closed (pool shut down)")
      }
      Behaviors.same
    }

  def responseToJson(response: ChatRoomActor.Response): JsValue =
    response match {
      case ChatRoomActor.Joined(name, members) =>
        Json.obj("type" -> "joined", "name" -> name, "members" -> members)
      case ChatRoomActor.MembersList(members) =>
        Json.obj("type" -> "members", "members" -> members)
      case ChatRoomActor.MessageReceived(
            ChatRoomActor.ChatMessage(sender, content)
          ) =>
        Json.obj("sender" -> sender, "content" -> content)
    }

  implicit val chatMessageFormat: Format[ChatRoomActor.ChatMessage] =
    Json.format[ChatRoomActor.ChatMessage]
  implicit val messageReceivedFormat: Format[ChatRoomActor.MessageReceived] =
    Json.format[ChatRoomActor.MessageReceived]
  implicit val membersListFormat: Format[ChatRoomActor.MembersList] =
    Json.format[ChatRoomActor.MembersList]
  implicit val joinedFormat: Format[ChatRoomActor.Joined] =
    Json.format[ChatRoomActor.Joined]
}
