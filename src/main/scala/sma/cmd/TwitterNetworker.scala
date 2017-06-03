package sma.cmd

import akka.Done
import akka.actor._
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.pattern.ask
import akka.stream.scaladsl.{Sink, Source}
import sma.Redis.Interests
import sma.cmd.DiggingMessages._
import sma.reactive.ReactiveWrappedActor
import sma.{Committing, Receiving}

import scala.concurrent.Future
import scala.concurrent.duration._

object TwitterNetworker {
  def props(): Props = {
    Props(classOf[TwitterNetworker])
  }
}

case class Heartbeat()

case class HeartbeatReply()

class TwitterNetworker(val topic: String) extends ReactiveWrappedActor with Receiving with Committing {

  val heartbeatPeriod = 2 seconds

  override def receive = {
    case bulk: DiggingBulk =>
      bulkProccess(bulk)
      sender() ! DiggingBulkReply()
    case _: Heartbeat =>
      streamFromTwitter()
      sender() ! HeartbeatReply()
  }

  override def preStart: Unit = {
    makeItReactive
    heartbeat
  }

  override def consume: Future[Done] = consumeAndBatchAsync

  private def heartbeat: Unit = {
    Source.tick(0 milliseconds, heartbeatPeriod, ())
      .async
      .runWith(Sink.foreach(_ => self ? Heartbeat()))
  }

  private def consumeAndBatchAsync: Future[Done] = {
    Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))
      .map(msg => Digging.deserialize(msg.value()))
      .groupedWithin(batchSize, batchPeriod)
      .mapAsync(1)(bulk => self ? DiggingBulk(bulk))
      .runWith(Sink.ignore)
  }

  private def bulkProccess(bulk: DiggingBulk): Future[Done] = {
    log.info(s"--> [${self.path.name}] receiving ${bulk.serialize}")
    Source(bulk().toVector)
      .runWith(Sink.foreach[Digging](proccess))
  }

  private def proccess(dig: Digging): Unit = {
    dig match {
      case Follow(_, _) => Interests.add(topic, dig.followee)
      case Forget(_, _) => Interests.remove(topic, dig.followee)
    }
  }

  private def streamFromTwitter(): Unit = {
    log.info(s"--> [${self.path.name}] streaming from twitter ${topic}")
    Source.fromFuture[Seq[String]](Interests(topic))
      .map(interest => interest.mkString.toUpperCase)
      .runWith(Sink.foreach(tweet => kafkaProducer.send(kafkaProducerRecord(replyTopic(topic), topic, tweet))))
  }
}

