package sma.cmd

import akka.actor._
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Sink
import sma.Receiving
import sma.cmd.DiggingMessages.Follow

import akka.pattern.ask
import scala.concurrent.duration._
import scala.util.{Success, Failure}

object Twitter {
  def props(): Props = {
    Props(classOf[Twitter])
  }
}

class Twitter(val topic: String) extends Actor with ActorLogging with Receiving {

  implicit val timeout = akka.util.Timeout(5 seconds)

  println(s"--> creating actor ${self.path.name}")

  override def receive = {
    case dig: Follow =>
      log.info(s"--> [${self.path.name}] receiving following message ${dig.mkString}")
      println(s"--> [${self.path.name}] receiving following message ${dig.mkString}")
      sender() ! dig.reply
    case _ =>
      println(s"--> [${self.path.name}] receiving an unknown message")
      sender() ! Some()
  }

  override def preStart: Unit = {
    println("--> creating Twitter stream")
    val processingActor = self
    val done = Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))
      .mapAsync(1)(msg => processingActor ? msg)
      .runWith(Sink.ignore)
    done.onComplete {
      case Failure(ex) =>
        log.error(ex, s"--> [${self.path.name}] stream failed, stopping the actor")
        self ! PoisonPill
      case Success(_) =>
        log.info(s"--> [${self.path.name}] gracefully shutdown")
    }

  }

  override def postStop(): Unit = {
    println(s"--> [${self.path.name}] actor stopped")
  }
}

object StreamWrapperTwitter {
  def create(implicit system: ActorSystem, childName: String): ActorRef = {
    val name: String = s"supervisor_of_${childName}_${java.util.UUID.randomUUID.toString}"

    println(s"--> creating ${name}")

    import akka.pattern.{Backoff, BackoffSupervisor}

    val supervisorProps = BackoffSupervisor.props(
      Backoff.onStop(
        Props(new Twitter(childName)),
        childName = childName,
        minBackoff = 3 seconds,
        maxBackoff = 30 seconds,
        randomFactor = 0.2
      )
    )
    val supervisor = system.actorOf(supervisorProps, name = name)

    supervisor
  }
}
