package sma.twitter

import akka.Done
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import sma.eventsourcing.Receiving
import sma.json.Json
import sma.reactive.ReactiveWrappedActor
import sma.storing.Redis.redis

import scala.concurrent.Future

object TwitterFeeder {
  val nick = "twitter_feeder"
}

class TwitterFeeder(topic: String) extends ReactiveWrappedActor with Receiving {

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
        redis.lpush(tweet.hashTrackingTerms, tweet.body)
      }
    })
  }

  private def consumeLinearAsync: Future[Done] = {
    val consumerGroup = s"${self.path.name}__twitter_feeder"
    plainSource(topic, consumerGroup)
      .mapAsync(1)(record => self ? tweet(record))
      .runWith(Sink.ignore)
  }

  private def tweet(record: ConsumerRecordType): Tweet = {
    Json.decode[Tweet](record.value())
  }

}
