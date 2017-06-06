package sma.twitter

import akka.actor._
import akka.pattern.ask
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.producer.ProducerRecord
import sma.digging.{BulkDigging, DiggingReactive}
import sma.eventsourcing.{TrackingTerms, Committing}
import sma.json.Json

import scala.concurrent.duration._

object TwitterNetworker {
  val nick = "twitter_networker"
}

class TwitterNetworker(val topic: String) extends DiggingReactive(topic) with Committing {

  val heartbeatPeriod = 2 seconds

  override def receive = {
    case heartbeat: Heartbeat =>
      if (trackingTerms.size > 0) {
        streamFromTwitter
      }
      sender() ! HeartbeatReply()
    case bulk: BulkDigging =>
      super.proccess(bulk)
  }

  override def preStart: Unit = {
    heartbeat
    super.preStart
  }

  override def consumerGroup = s"${self.path.name}_${TwitterNetworker.nick}"

  private def heartbeat: Unit = {
    Source.tick(0 milliseconds, heartbeatPeriod, ())
      .async
      .runWith(Sink.foreach(_ => self ? Heartbeat()))
  }

  private def streamFromTwitter(): Unit = {
    val ttt = trackingTermsTopic(replyTopic(topic), trackingTerms.toSeq)
    log.info(s"--> [${self.path.name}] streaming from twitter to |${ttt}| w/ terms: (${trackingTerms.mkString(", ")})")


    // TODO: bring them from twitter instead !
    Source(trackingTerms.toVector)
      .map(term => Tweet(term.toUpperCase, trackingTerms.toSeq, timestamp))
      .runWith(Sink.foreach(tweet => {
        producer.send(twitterProducerRecord(tweet, ttt))
        producer.send(twitterProducerRecord(tweet, replyTopic(topic)))
      }))
  }

  private def twitterProducerRecord(tweet: Tweet, targetTopic: String) = {
    val key = Json.encode(TrackingTerms(tweet.trackingTerms))
    val value = Json.encode(tweet)
    new ProducerRecord[Array[Byte], Array[Byte]](targetTopic, key, value)
  }

}

