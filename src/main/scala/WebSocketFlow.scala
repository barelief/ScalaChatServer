// WebSocketFlow.scala
// Manages WebSocket connections and message flow between clients and chat room

package bare.chatserver

// Import required Akka and JSON libraries
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{OverflowStrategy, QueueOfferResult}
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Promise}

object WebSocketFlow extends Logging {
  // Creates bidirectional WebSocket flow for chat messages
  def create(name: String, chatRoom: ActorRef[ChatRoomActor.Command])
            (implicit system: ActorSystem[?], ec: ExecutionContext): Flow[Message, Message, Any] = {
    // Create message queue with overflow strategy
    val (queue, publisher) = Source
      .queue[ChatRoomActor.Response](bufferSize = 100, OverflowStrategy.dropHead)
      .preMaterialize()

    // Set up promise for actor reference
    val responsePromise = Promise[ActorRef[ChatRoomActor.Response]]()
    
    // Create actor for handling JSON messages
    val actorRef = system.systemActorOf(
      JsonMessageAdapter.behavior(queue),
      s"user-$name-${System.currentTimeMillis()}" 
    )
    responsePromise.success(actorRef)

    // Set up outgoing message stream
    val outgoingMessages = Source.futureSource(responsePromise.future.map { actorRef =>
      chatRoom ! ChatRoomActor.Join(name, actorRef)
      // Handle stream termination 
      publisher.watchTermination()((_, done) => {
        done.foreach(_ => chatRoom ! ChatRoomActor.Leave(name, actorRef))
        ()
      })
      publisher
    })

    // Handle incoming messages
    val incoming = Flow[Message]
      .collect { case TextMessage.Strict(text) => Json.parse(text) }
      .collect {
        case json if (json \ "content").asOpt[String].isDefined =>
          val content = (json \ "content").as[String]
          ChatRoomActor.Message(name, content)
      }
      .to(Sink.foreach[ChatRoomActor.Command](chatRoom ! _))

    // Convert outgoing messages to JSON
    val outgoing = outgoingMessages.map { response =>
      val json = JsonMessageAdapter.responseToJson(response)
      TextMessage(Json.stringify(json))
    }

    // Create bidirectional flow
    Flow.fromSinkAndSource(incoming, outgoing)
  }
}
