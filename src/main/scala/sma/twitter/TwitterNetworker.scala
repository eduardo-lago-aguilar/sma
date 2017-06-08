package sma.twitter

import akka.actor._
import akka.pattern.ask
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.producer.ProducerRecord
import sma.Settings
import sma.digging.{BulkDigging, BulkDiggingReply, DiggingReactive}
import sma.eventsourcing.{Committing, TrackingTerms}
import sma.json.Json

import scala.concurrent.duration._

object TwitterNetworker {
  val nick = "twitter_networker"
}

class TwitterNetworker(val topic: String) extends DiggingReactive(topic) with Committing {

  val heartbeatPeriod = 15 seconds
  val maxNumberOfTweets = 10

  override def receive = {
    case heartbeat: Heartbeat =>
      sender() ! HeartbeatReply()
      streamFromTwitter
    case bulk: BulkDigging =>
      super.proccess(bulk)
      sender() ! BulkDiggingReply()
      streamFromTwitter
  }

  override def preStart: Unit = {
    heartbeat
    super.preStart
  }

  override def consumerGroup = s"${self.path.name}_${TwitterNetworker.nick}"

  private def heartbeat: Unit = {
    Source.tick(0 milliseconds, heartbeatPeriod, ())
      .async
      .runForeach(_ => self ? Heartbeat())
  }

  private def streamFromTwitter(): Unit = {
    if (trackingTerms.size > 0) {
      log.info(s"--> [${self.path.name}] streaming from twitter to |${topic}| w/ terms: (${trackingTerms.mkString(", ")})")

      val tweetSource = new TweetSource(Settings.oAuth1, trackingTerms.toVector)

      val count: Int = tweetSource.iterate(maxNumberOfTweets, storeTweet)

      log.info(s"--> [${self.path.name}] finishing streaming from twitter to |${topic}| w/ terms: (${trackingTerms.mkString(", ")}), brought ${count} messages!")

      tweetSource.close
    }
  }

  private def storeTweet(json: String) = {
    val id: String = Json.Tweet.decodeId(json)

    val tweet = Tweet(id, json, trackingTerms.toSeq, timestamp)
    log.info(s"storing tweet with ID = ${id}")

    producer.send(twitterProducerRecord(tweet, replyTopic(topic)))
  }

  private def twitterProducerRecord(tweet: Tweet, targetTopic: String) = {
    val key = Json.encode(TrackingTerms(tweet.trackingTerms))
    val value = Json.encode(tweet)
    new ProducerRecord[Array[Byte], Array[Byte]](targetTopic, key, value)
  }

}

