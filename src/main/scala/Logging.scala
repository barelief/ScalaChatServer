// Logging.scala
// The following code defines a Logging trait and Logging object 
package bare.chatserver

// import necessary libraries
import akka.actor.typed.ActorSystem // for creating the actor system
import org.slf4j.Logger  // SLF4J Logger interface for logging functionality
import org.slf4j.LoggerFactory // Factory for creating logger instances

// Define the Logging trait
trait Logging {
  // Create a logger instance using the class name
  def logger: Logger = LoggerFactory.getLogger(getClass)
}

// Companion object for Logging that provides an alternative way to create loggers
object Logging {
  def apply(system: ActorSystem[?]): Logger = system.log
}
