package sma.cmd

import akka.actor._
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import sma.Redis.Interests
import sma.cmd.DiggingMessages._
import sma.{Receiving, StreamWrapper}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Twitter {
  def props(): Props = {
    Props(classOf[Twitter])
  }
}

class Twitter(val topic: String) extends Actor with ActorLogging with Receiving {

  implicit val timeout = akka.util.Timeout(5 seconds)

  println(s"--> [${self.path.name}] creating actor ")

  override def receive = {
    case digVector: Vector[Follow] =>
      val followees = digVector.map(v => v.followee)
      Interests.add(topic, followees: _*)
      sender() ! FollowReply()
      logFollowees(followees, "follow")
    case digVector: Vector[Forget] =>
      val followees = digVector.map(v => v.followee)
      Interests.remove(topic, followees: _*)
      sender() ! ForgetReply()
      logFollowees(followees, "forget")
    case _ =>
      println(s"--> [${self.path.name}] receiving an unknown message")
      sender() ! Some()
  }

  def logFollowees(followees: Vector[String], request: String): Unit = {
    log.info(s"--> [${self.path.name}] receiving [${followees.reduce((t, c) => s"${c}, ${t}")}] ${request} request")
  }

  override def preStart: Unit = {
    println(s"--> [${self.path.name}] creating twitter stream")
    val processingActor = self
    val done = Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))
      .map(msg => Digging.deserialize(msg.value()))
      .groupedWithin(100, 2 seconds)
      .mapAsync(1)(bulk => processingActor ? bulk)
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

object StreamWrapperTwitter extends StreamWrapper {
  override def create(implicit system: ActorSystem, childName: String): ActorRef = {
    val name = s"supervisor_of_${childName}"

    println(s"--> [${name}] creating supervisor")

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
