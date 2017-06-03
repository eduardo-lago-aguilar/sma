package sma

import akka.actor._
import akka.http.scaladsl.server.HttpApp
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.utils.Bytes
import sma.cmd.Commands

import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class ProcessMsg(msg: ConsumerRecord[String, Bytes])

class StreamWrapperActor extends Actor with Receiving with ActorLogging {

  implicit val timeout = akka.util.Timeout(5 seconds)

  def receive = {
    case ProcessMsg(msg) =>
      println(s"receiving message: ${msg.value()}")
      // message processing
      sender() ! msg
  }

  def createStream(): Unit = {
    println("creating stream !!!")

    val processingActor = self
    //#errorHandlingStop
    val done =
      Consumer.plainSource(consumerSettings, Subscriptions.topics("topicY"))
        .mapAsync(1)(msg => processingActor ? ProcessMsg(msg))
        .runWith(Sink.ignore)

    done.onComplete {
      case Failure(ex) =>
        log.error(ex, "Stream failed, stopping the actor.")
        self ! PoisonPill
      case Success(_) => // gracceful stream shutdown handling
        log.info("gracceful stream shutdown handling")
    }
    //#errorHandlingStop
  }
  createStream()

}

object StreamWrapperActor {

  def create(implicit system: ActorSystem): ActorRef = {
    println("supervising actor !!!")
    //#errorHandlingSupervisor
    import akka.pattern.{Backoff, BackoffSupervisor}

    val childProps = Props(classOf[StreamWrapperActor])

    val supervisorProps = BackoffSupervisor.props(
      Backoff.onStop(
        childProps,
        childName = "streamActor",
        minBackoff = 3.seconds,
        maxBackoff = 30.seconds,
        randomFactor = 0.2
      )
    )
    val supervisor = system.actorOf(supervisorProps, name = "streamActorSupervisor")
    //#errorHandlingSupervisor
    supervisor
  }
}

object KafkaReactiveMainExample extends HttpApp with App with Commands {

  implicit val digger = null

  val supervisor: ActorRef = StreamWrapperActor.create(system)

  startServer("localhost", 8080)

  def routes = commandRoutes
}

