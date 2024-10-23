package bare.chatserver

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{OverflowStrategy, QueueOfferResult}
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Promise}

object WebSocketFlow extends Logging {
  def create(name: String, chatRoom: ActorRef[ChatRoomActor.Command])
            (implicit system: ActorSystem[?], ec: ExecutionContext): Flow[Message, Message, Any] = {
    val (queue, publisher) = Source
      .queue[ChatRoomActor.Response](bufferSize = 100, OverflowStrategy.dropHead)
      .preMaterialize()

    val responsePromise = Promise[ActorRef[ChatRoomActor.Response]]()

    val actorRef: ActorRef[ChatRoomActor.Response] = system.systemActorOf(
      JsonMessageAdapter.behavior(queue),
      s"user-$name"
    )

    responsePromise.success(actorRef)

    val outgoingMessages = Source.futureSource(responsePromise.future.map { actorRef =>
      // logger.info(s"User $name connected")
      chatRoom ! ChatRoomActor.Join(name, actorRef)
      publisher
    })

    val incoming = Flow[Message]
      .collect { case TextMessage.Strict(text) => Json.parse(text) }
      .collect {
        case json if (json \ "content").asOpt[String].isDefined =>
          val content = (json \ "content").as[String]
          // logger.info(s"Incoming message from $name: $content")
          ChatRoomActor.Message(name, content)
      }
      .to(Sink.foreach[ChatRoomActor.Command](chatRoom ! _))

    val outgoing = outgoingMessages.map { response =>
      val json = JsonMessageAdapter.responseToJson(response)
      TextMessage(Json.stringify(json))
    }

    Flow.fromSinkAndSource(incoming, outgoing)
  }
}
