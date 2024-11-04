// JsonMessageAdapter.scala
// Handles conversion of chat messages to/from JSON format and manages message queuing for WebSocket communication

package bare.chatserver

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.SourceQueueWithComplete
import akka.stream.QueueOfferResult
import play.api.libs.json._
import scala.concurrent.ExecutionContext

object JsonMessageAdapter extends Logging {
  // Actor behavior that handles enqueueing messages to WebSocket stream 
  def behavior(queue: SourceQueueWithComplete[ChatRoomActor.Response])(implicit
      ec: ExecutionContext
  ): Behavior[ChatRoomActor.Response] =
    Behaviors.receiveMessage { msg =>
     // Try to enqueue message and handle different queue states 
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

  // Convert chat responses to JSON format  
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

  // JSON format definitions for chat message typesP
  implicit val chatMessageFormat: Format[ChatRoomActor.ChatMessage] =
    Json.format[ChatRoomActor.ChatMessage]
  implicit val messageReceivedFormat: Format[ChatRoomActor.MessageReceived] =
    Json.format[ChatRoomActor.MessageReceived]
  implicit val membersListFormat: Format[ChatRoomActor.MembersList] =
    Json.format[ChatRoomActor.MembersList]
  implicit val joinedFormat: Format[ChatRoomActor.Joined] =
    Json.format[ChatRoomActor.Joined]
}
