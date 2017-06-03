package sma.cmd

import akka.actor._
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.pattern.ask
import akka.stream.scaladsl.{Source, Sink}
import sma.Redis.Interests
import sma.cmd.DiggingMessages._
import sma.{Committing, Receiving, StreamWrapper}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Twitter {
  def props(): Props = {
    Props(classOf[Twitter])
  }
}

class Twitter(val topic: String) extends Actor with ActorLogging with Receiving with Committing {

  implicit val timeout = akka.util.Timeout(5 seconds)

  println(s"--> [${self.path.name}] creating actor ")

  override def receive = {
    case bulk: DiggingBulk =>
      Source(bulk().toVector)
        .runWith(Sink.foreach[Digging](proccess))
      sender() ! DiggingBulkReply()
      logMessages(bulk.serialize)
      streamFromTwitter()
    case _ =>
      println(s"--> [${self.path.name}] receiving an unknown message")
      sender() ! Some()
  }

  private def proccess(dig: Digging): Unit = {
    dig match {
      case Follow(_, _) => Interests.add(topic, dig.followee)
      case Forget(_, _) => Interests.remove(topic, dig.followee)
    }
  }

  private def streamFromTwitter(): Unit = {
    Source.fromFuture[Seq[String]](Interests(topic))
      .map(interest => interest.mkString.toUpperCase)
      .runWith(Sink.foreach(tweet => kafkaProducer.send(kafkaProducerRecord(replyTopic(topic), topic, tweet))))
  }

  private def logMessages(followees: String): Unit = {
    log.info(s"--> [${self.path.name}] receiving ${followees}")
  }

  override def preStart: Unit = {
    println(s"--> [${self.path.name}] creating twitter stream")
    val processingActor = self
    val done = Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))
      .map(msg => Digging.deserialize(msg.value()))
      .groupedWithin(100, 2 seconds)
      .mapAsync(1)(bulk => processingActor ? DiggingBulk(bulk))
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
