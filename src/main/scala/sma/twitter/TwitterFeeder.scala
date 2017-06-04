package sma.twitter

import akka.Done
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import sma.reactive.ReactiveWrappedActor

import scala.concurrent.Future

class TwitterFeeder(topic: String) extends ReactiveWrappedActor with Twitter {

  override def receive = {
    case tweet: Tweet =>
      log.info(s"--> [${self.path.name}] receiving tweet ${tweet.body}")
      sender() ! TweetReply()
  }

  override def consume: Future[Done] = consumeLinearAsync

  private def consumeLinearAsync: Future[Done] = {
    twitterPlainSource(topic)
      .mapAsync(1)(record => self ? tweet(record))
      .runWith(Sink.ignore)
  }


}
