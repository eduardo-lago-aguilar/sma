package sma.twitter

import akka.Done
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import sma.json.Json
import sma.eventsourcing.Receiving
import sma.reactive.ReactiveWrappedActor

import scala.concurrent.Future

class TwitterFeeder(topic: String) extends ReactiveWrappedActor with Receiving {

  override def receive = {
    case tweet: Tweet =>
      log.info(s"--> [${self.path.name}] receiving tweet ${tweet.body}")
      sender() ! TweetReply()
  }

  override def consume: Future[Done] = consumeLinearAsync

  private def consumeLinearAsync: Future[Done] = {
    plainSource(topic)
      .mapAsync(1)(record => self ? tweet(record))
      .runWith(Sink.ignore)
  }

  private def tweet(record: ConsumerRecordType): Tweet = {
    Json.decode[Tweet](record.value())
  }

}
