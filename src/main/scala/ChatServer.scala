package bare.chatserver
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives.*
import akka.stream.scaladsl.{Flow, Source, Sink, SourceQueueWithComplete, Keep}
import akka.stream.{OverflowStrategy, QueueOfferResult}
import play.api.libs.json.*

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Success, Failure}

object ClaudeChatServer:
  // ADT for ChatRoom messages
  enum ChatRoomCommand:
    case Join(name: String, replyTo: ActorRef[Response])
    case Leave(name: String)
    case Message(sender: String, content: String)

  // ADT for Response messages
  enum Response:
    case Joined(members: Set[String])
    case MessageReceived(message: ChatMessage)

  // Case class for chat messages
  case class ChatMessage(sender: String, content: String)

  // JSON formats
  given Format[ChatMessage] = Json.format[ChatMessage]
  given Format[Response.MessageReceived] = Json.format[Response.MessageReceived]

  // ChatRoom actor behavior (manages users and broadcast messages)
  def chatRoom(): Behavior[ChatRoomCommand] =
    // Internal method for managing the state of the chat room
    def updated(
        members: Set[String],
        subscribers: Set[
          ActorRef[Response]
        ] // Set of subscribers (actors) to notify of messages
    ): Behavior[ChatRoomCommand] =
      // Behavior when receiving a chat room command
      Behaviors.receiveMessage {
        case ChatRoomCommand.Join(name, replyTo) =>
          subscribers.foreach(
            _ ! Response.Joined(members + name)
          )
          updated(members + name, subscribers + replyTo)

        case ChatRoomCommand.Leave(name) =>
          updated(members - name, subscribers)

        case ChatRoomCommand.Message(sender, content) =>
          val message = Response.MessageReceived(ChatMessage(sender, content))
          subscribers.foreach(_ ! message)
          Behaviors.same
      }

    updated(Set.empty, Set.empty)

  @main def run(): Unit =
    // Create an ActorSystem to manage actors
    given system: ActorSystem[Nothing] =
      ActorSystem(Behaviors.empty, "chat-system")
    given ExecutionContext = system.executionContext

    // Create a chat room actor that handles chat-related commands
    val chatRoomActor = system.systemActorOf(chatRoom(), "chat-room")

    // Define a WebSocket flow for handling communication between a user and the chat room
    def websocketFlow(name: String): Flow[Message, Message, Any] =
      // Source to queue responses and broadcast them to users
      val (queue, publisher) = Source
        .queue[Response](bufferSize = 100, OverflowStrategy.dropHead)
        .preMaterialize()

      // Create a promise to hold the user's actor reference
      val responsePromise = Promise[ActorRef[Response]]()

      // Actor that handles the user's chat responses
      val actorRef: ActorRef[Response] = system.systemActorOf(
        Behaviors.receiveMessage[Response] { msg =>
          queue.offer(msg).foreach {
            case QueueOfferResult.Enqueued => ()
            case QueueOfferResult.Dropped  => println(s"Dropped message: $msg")
            case QueueOfferResult.Failure(ex) => println(s"Enqueue failed: $ex")
            case QueueOfferResult.QueueClosed =>
              println("Queue was closed (pool shut down)")
          }
          Behaviors.same
        },
        s"user-$name"
      )

      // Fulfill the promise with the user's actor reference
      responsePromise.success(actorRef)

      // Outgoing message source (future) for sending chat room updates to the user
      val outgoingMessages = Source.futureSource(responsePromise.future.map {
        actorRef =>
          // Join the chat room and start publishing messages
          chatRoomActor ! ChatRoomCommand.Join(name, actorRef)
          publisher
      })

      // Incoming message flow for receiving user messages and sending them to the chat room
      val incoming = Flow[Message]
        .collect { case TextMessage.Strict(text) =>
          val json = Json.parse(text)
          (json \ "content").as[String]
        }
        .map(content => ChatRoomCommand.Message(name, content))
        .to(Sink.foreach[ChatRoomCommand](chatRoomActor ! _))

      // Outgoing flow for sending chat updates back to the client
      val outgoing = outgoingMessages
        .map {
          case Response.Joined(members) =>
            TextMessage(
              Json.obj("type" -> "members", "members" -> members).toString
            )
          case Response.MessageReceived(message) =>
            TextMessage(Json.toJson(message).toString)
        }

      // Combine incoming and outgoing flows into one flow
      Flow.fromSinkAndSource(incoming, outgoing)

    // Define the route for WebSocket connections
    val route =
      path("ws-chat" / Segment) { name =>
        handleWebSocketMessages(websocketFlow(name))
      }

    // Start the HTTP server
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    println(s"Server online at http://localhost:8080/")
    println(s"Press RETURN to stop...")
    scala.io.StdIn.readLine()

    // Unbind the server and terminate the actor system
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
