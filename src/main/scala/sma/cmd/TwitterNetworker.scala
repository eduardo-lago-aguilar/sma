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

class TwitterNetworker(val topic: String) extends ReactiveWrappedActor with Receiving with Committing {

  override def receive = {
    case bulk: DiggingBulk =>
      Source(bulk().toVector)
        .runWith(Sink.foreach[Digging](proccess))
      sender() ! DiggingBulkReply()
      log.info(s"--> [${self.path.name}] receiving ${bulk.serialize}")
      streamFromTwitter()
  }

  override def consume: Future[Done] = consumeAndBatchAsync

  private def consumeAndBatchAsync: Future[Done] = {
    Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))
      .map(msg => Digging.deserialize(msg.value()))
      .groupedWithin(100, 2 seconds)
      .mapAsync(1)(bulk => self ? DiggingBulk(bulk))
      .runWith(Sink.ignore)
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
}

