// ChatRoomActor.scala
// The following code defines the ChatRoomActor, 
// which manages the chat room state 
// and handles all chat-related commands. 

package bare.chatserver
// import necessary Akka Typed libraries
import akka.actor.typed.{ActorRef, Behavior} // for defining the actor behavior
import akka.actor.typed.scaladsl.Behaviors // for creating the behavior
import bare.chatserver.SaveDataModule // for saving chat data

// ChatRoomActor manages the chat room state and handles all chat-related command
object ChatRoomActor {
  // Define the command protocol - all possible messages this actor can receive 
  sealed trait Command

  // Command messages
  final case class Join(name: String, replyTo: ActorRef[Response]) extends Command
  final case class Leave(name: String, replyTo: ActorRef[Response]) extends Command
  final case class Message(sender: String, content: String) extends Command
  final case class GetMembers(replyTo: ActorRef[Response]) extends Command

  // Define the response protocol - all possible messages this actor can send
  sealed trait Response

  final case class Joined(name: String, members: Set[String]) extends Response
  final case class MembersList(members: Set[String]) extends Response
  final case class MessageReceived(message: ChatMessage) extends Response
  
  // Define the chat message structure
  case class ChatMessage(sender: String, content: String)
  
  // Define the UserConnection structure
  private case class UserConnection(name: String, ref: ActorRef[Response])
  
  // Define the behavior for the ChatRoomActor
  // Main entry point for creating instances of the actor
  def behavior(): Behavior[Command] = Behaviors.setup { context =>
    val log = context.log    

    /**
    * Internal state handler that maintains the set of active connections
    * @param connections Set of active user connections
    */
    def updated(
        connections: Set[UserConnection]
    ): Behavior[Command] = {
     
      // Helper function to get the names of active members
      def activeMembers: Set[String] = connections.map(_.name)
      
      // Define the message handling logic
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

          // Save the chat data
          SaveDataModule.saveData(sender, content)
          
          // Broadcast the message to all active connections
          val message = MessageReceived(ChatMessage(sender, content))
          connections.foreach(_.ref ! message)
          
          Behaviors.same

        case GetMembers(replyTo) =>
          log.info(s"Members list requested")
          replyTo ! MembersList(activeMembers)
          Behaviors.same
      }
    }
    // Start with an empty set of connections
    updated(Set.empty)
  }
}
