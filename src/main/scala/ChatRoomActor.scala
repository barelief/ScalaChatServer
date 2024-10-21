package bare.chatserver

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object ChatRoomActor {
  sealed trait Command
  final case class Join(name: String, replyTo: ActorRef[Response])
      extends Command
  final case class Leave(name: String) extends Command
  final case class Message(sender: String, content: String) extends Command
  final case class GetMembers(replyTo: ActorRef[Response]) extends Command

  sealed trait Response
  final case class Joined(name: String, members: Set[String]) extends Response
  final case class MembersList(members: Set[String]) extends Response
  final case class MessageReceived(message: ChatMessage) extends Response

  case class ChatMessage(sender: String, content: String)

  def behavior(): Behavior[Command] = Behaviors.setup { context =>
    val log = Logging(context.system)

    def updated(
        members: Set[String],
        subscribers: Set[ActorRef[Response]]
    ): Behavior[Command] =
      Behaviors.receiveMessage {
        case Join(name, replyTo) =>
          log.info(s"User $name joined the chat")
          val updatedMembers = members + name
          subscribers.foreach(_ ! Joined(name, updatedMembers))
          replyTo ! MembersList(updatedMembers)
          updated(updatedMembers, subscribers + replyTo)

        case Leave(name) =>
          log.info(s"User $name left the chat")
          val updatedMembers = members - name
          subscribers.foreach(_ ! MembersList(updatedMembers))
          updated(updatedMembers, subscribers)

        case Message(sender, content) =>
          log.info(s"$sender: $content")
          val message = MessageReceived(ChatMessage(sender, content))
          subscribers.foreach(_ ! message)
          Behaviors.same

        case GetMembers(replyTo) =>
          log.info(s"Members list requested")
          replyTo ! MembersList(members)
          Behaviors.same
      }

    updated(Set.empty, Set.empty)
  }
}
