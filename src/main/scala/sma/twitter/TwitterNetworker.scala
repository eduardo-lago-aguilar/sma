package sma.twitter

import akka.actor._
import akka.pattern.ask
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.producer.ProducerRecord
import sma.digging.{BulkDigging, DiggingReactive}
import sma.eventsourcing.Committing
import sma.feeding.TrackingTerms
import sma.json.Json

import scala.concurrent.duration._

class TwitterNetworker(val topic: String) extends DiggingReactive(topic) with Committing {

  val heartbeatPeriod = 2 seconds

  override def receive = {
    case heartbeat: Heartbeat =>
      streamFromTwitter()
      sender() ! HeartbeatReply()
    case bulk: BulkDigging =>
      super.proccess(bulk)
  }

  override def preStart: Unit = {
    heartbeat
    super.preStart
  }

  private def heartbeat: Unit = {
    Source.tick(0 milliseconds, heartbeatPeriod, ())
      .async
      .runWith(Sink.foreach(_ => self ? Heartbeat()))
  }

  private def streamFromTwitter(): Unit = {
    log.info(s"--> [${self.path.name}] streaming from twitter to |${replyTopic(topic)}|")

    // TODO: bring them from twitter instead !
    Source(trackingTerms.toVector)
      .map(term => Tweet(term.toUpperCase, trackingTerms.toSeq, timestamp))
      .runWith(Sink.foreach(tweet => producer.send(twitterProducerRecord(tweet, replyTopic(topic)))))
  }

  private def twitterProducerRecord(tweet: Tweet, topic: String) = {
    val key = Json.encode(TrackingTerms(tweet.trackingTerms))
    val value = Json.encode(tweet)
    new ProducerRecord[Array[Byte], Array[Byte]](topic, key, value)
  }

}

