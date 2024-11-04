package bare.chatserver

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import bare.chatserver.SaveDataModule

object ChatRoomActor {
  sealed trait Command
  final case class Join(name: String, replyTo: ActorRef[Response]) extends Command
  final case class Leave(name: String, replyTo: ActorRef[Response]) extends Command
  final case class Message(sender: String, content: String) extends Command
  final case class GetMembers(replyTo: ActorRef[Response]) extends Command

  sealed trait Response
  final case class Joined(name: String, members: Set[String]) extends Response
  final case class MembersList(members: Set[String]) extends Response
  final case class MessageReceived(message: ChatMessage) extends Response

  case class ChatMessage(sender: String, content: String)
  
  private case class UserConnection(name: String, ref: ActorRef[Response])

  def behavior(): Behavior[Command] = Behaviors.setup { context =>
    val log = context.log    

    def updated(
        connections: Set[UserConnection]
    ): Behavior[Command] = {
      
      def activeMembers: Set[String] = connections.map(_.name)
      
      Behaviors.receiveMessage {
        case Join(name, replyTo) =>
          log.info(s"User $name joined the chat")
          
          // Add new connection first
          val newConnection = UserConnection(name, replyTo)
          val updatedConnections = connections + newConnection
          
          // Calculate new members list including the just-added connection
          val currentMembers = updatedConnections.map(_.name)
          
          // Send current members list to the new connection
          replyTo ! MembersList(currentMembers)
          
          // Notify all existing connections about the join if this is the first connection for this username
          if (!connections.exists(_.name == name)) {
            connections.foreach(_.ref ! Joined(name, currentMembers))
          }
          
          updated(updatedConnections)

        case Leave(name, replyTo) =>
          log.info(s"Connection closed for user $name")
          
          // Remove specific connection
          val updatedConnections = connections.filterNot(conn => 
            conn.name == name && conn.ref == replyTo
          )
          
          // Only notify others if this was the last connection for this username
          if (!updatedConnections.exists(_.name == name)) {
            val remainingMembers = updatedConnections.map(_.name)
            updatedConnections.foreach(_.ref ! MembersList(remainingMembers))
            log.info(s"User $name left the chat (all connections closed)")
          }
          
          updated(updatedConnections)

        case Message(sender, content) =>
          log.info(s"$sender: $content")
          SaveDataModule.saveData(sender, content)
          
          val message = MessageReceived(ChatMessage(sender, content))
          connections.foreach(_.ref ! message)
          
          Behaviors.same

        case GetMembers(replyTo) =>
          log.info(s"Members list requested")
          replyTo ! MembersList(activeMembers)
          Behaviors.same
      }
    }

    updated(Set.empty)
  }
}
