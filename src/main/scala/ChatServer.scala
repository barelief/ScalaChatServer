package bare.chatserver

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext
import scala.util.{Success, Failure}

object ChatServer extends Logging {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] =
      ActorSystem(ChatRoomActor.behavior(), "chat-system")
    implicit val executionContext: ExecutionContext = system.executionContext

    val chatRoomActor =
      system.systemActorOf(ChatRoomActor.behavior(), "chat-room")

    val route = path("ws-chat" / Segment) { name =>
      handleWebSocketMessages(WebSocketFlow.create(name, chatRoomActor))
    }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    bindingFuture.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        logger.info(
          s"Server online at http://${address.getHostString}:${address.getPort}/"
        )
        logger.info(s"Press RETURN to stop...")
      case Failure(ex) =>
        logger.error(s"Failed to bind HTTP server: ${ex.getMessage}", ex)
        system.terminate()
    }

    scala.io.StdIn.readLine()

    bindingFuture
      .flatMap(_.unbind())
      .onComplete {
        case Success(_) =>
          logger.info("Server unbound successfully")
          system.terminate()
        case Failure(ex) =>
          logger.error(s"Error unbinding server: ${ex.getMessage}", ex)
          system.terminate()
      }
  }
}
