package bare.chatserver

import akka.actor.typed.ActorSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory

trait Logging {
  def logger: Logger = LoggerFactory.getLogger(getClass)
}

object Logging {
  def apply(system: ActorSystem[?]): Logger = system.log
}
