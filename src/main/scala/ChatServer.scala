// ChatServer.scala
// Code for the ChatServer object that starts the server 
//
package bare.chatserver 

//import necessary Akka HTTP and Akka Typed libraries
import akka.actor.typed.ActorSystem // for creating the actor system
import akka.http.scaladsl.Http // for creating the HTTP server
import akka.http.scaladsl.server.Directives._ // For defining the route
import scala.concurrent.ExecutionContext // for handling async operations

import scala.util.{Success, Failure} // for handling success and failure (for Future)

object ChatServer extends Logging {
  def main(args: Array[String]): Unit = {
    // Create the actor system with ChatRoomActor behavior
    // The [Nothing] type parameter indicates this system won't receive messages directly
    implicit val system: ActorSystem[Nothing] =
      ActorSystem(ChatRoomActor.behavior(), "chat-system")

    // Get the execution context from the actor system
    implicit val executionContext: ExecutionContext = system.executionContext

    // Create the main chat room actor that will handle all chat operations
    // This is a top-level actor in the system
    val chatRoomActor =
      system.systemActorOf(ChatRoomActor.behavior(), "chat-room")

    // Define the route for the WebSocket chat server
    val route = path("ws-chat" / Segment) { name =>
      handleWebSocketMessages(WebSocketFlow.create(name, chatRoomActor))
    }

    // Bind the server to localhost:8080
    // Returns a Future that will complete when the binding is done
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    // Handle the completion of the binding operation
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

    // Wait for user input to shut down the server
    scala.io.StdIn.readLine()

    // Shut down the server
    bindingFuture
      .flatMap(_.unbind()) // First unbind the server  
      .onComplete {       // Then terminate the actor system  
        case Success(_) =>
          logger.info("Server unbound successfully")
          system.terminate()
        case Failure(ex) =>
          logger.error(s"Error unbinding server: ${ex.getMessage}", ex)
          system.terminate()
      }
  }
}
