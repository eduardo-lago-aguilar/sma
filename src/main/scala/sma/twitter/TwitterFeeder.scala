package sma.twitter

import akka.Done
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import sma.eventsourcing.Receiving
import sma.json.Json
import sma.reactive.ReactiveWrappedActor
import sma.storing.Redis.MessagesStore

import scala.concurrent.Future

object TwitterFeeder {
  val nick = "twitter_feeder"
}

class TwitterFeeder(topic: String) extends ReactiveWrappedActor with Receiving {

  override def receive = {
    case tweet: Tweet =>
      val ttt: String = trackingTermsTopic(topic, tweet.trackingTerms)
      log.info(s"--> [${self.path.name}] receiving tweet -> storing message at redis ${ttt}")
      log.debug(tweet.body)
      MessagesStore.add(ttt, tweet.body)
      sender() ! TweetReply()
  }

  override def consume: Future[Done] = consumeLinearAsync

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
