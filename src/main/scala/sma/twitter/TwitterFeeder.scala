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
      receiving(s"receiving tweet with id ${tweet.id} -> storing message at redis ${trackingTermsTopic(topic, tweet.trackingTerms)}")
      storeTweet(tweet)
  }

  override def consume: Future[Done] = consumeLinearAsync

  private def storeTweet(tweet: Tweet) = {
    val ttt = trackingTermsTopic(topic, tweet.trackingTerms)
    val tttTweet = s"${ttt}_${tweet.id}"
    redis.exists(tttTweet).foreach(exists => {
      if (!exists) {
        redis.set(tttTweet, 1)
        redis.lpush(ttt, tweet.body)
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
