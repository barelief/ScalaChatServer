package bare.chatserver

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object ChatServer {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(ChatRoomActor.behavior(), "chat-system")
    implicit val executionContext: ExecutionContext = system.executionContext

    val chatRoomActor = system.systemActorOf(ChatRoomActor.behavior(), "chat-room")

    val route = path("ws-chat" / Segment) { name =>
      handleWebSocketMessages(WebSocketFlow.create(name, chatRoomActor))
    }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    println(s"Server online at http://localhost:8080/")
    println(s"Press RETURN to stop...")
    StdIn.readLine()

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}