package sma.twitter

import akka.Done
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.producer.ProducerRecord
import sma.eventsourcing.{Committing, Receiving, TrackingTerms}
import sma.json.Json
import sma.reactive.ReactiveWrappedActor
import sma.storing.Redis.redis

import scala.concurrent.Future

object TwitterFeeder {
  val nick = "twitter_feeder"
}

class TwitterFeeder(topic: String) extends ReactiveWrappedActor with Receiving with Committing {

  override def receive = {
    case tweet: Tweet =>
      sender() ! TweetReply()
      logReceiving(s"receiving tweet with id ${tweet.id} -> storing message at redis ${tweet.hashTrackingTerms}")
      storeTweet(tweet)
  }

  override def consume: Future[Done] = consumeLinearAsync

  private def storeTweet(tweet: Tweet) = {
    val httId: String = s"${tweet.hashTrackingTerms}_${tweet.id}"
    redis.exists(httId).foreach(exists => {
      if (!exists) {
        redis.set(httId, 1)
        producer.send(twitterProducerRecord(tweet, tweet.hashTrackingTerms))
      }
    })
  }

  private def consumeLinearAsync: Future[Done] = {
    val consumerGroup = s"${self.path.name}__twitter_feeder"
    plainSource(topic, consumerGroup)
      .mapAsync(1)(record => self ? decodeTweet(record))
      .runWith(Sink.ignore)
  }

  private def twitterProducerRecord(tweet: Tweet, targetTopic: String) = {
    val key = Json.encode(TrackingTerms(tweet.trackingTerms))
    val value = Json.encode(tweet)
    new ProducerRecord[Array[Byte], Array[Byte]](targetTopic, key, value)
  }


}
