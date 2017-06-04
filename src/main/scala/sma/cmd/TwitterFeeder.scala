package sma.cmd

import akka.Done
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerRecord
import sma.reactive.ReactiveWrappedActor
import sma.{Receiving, Tweet, TweetReply}

import scala.concurrent.Future

class TwitterFeeder(topic: String) extends ReactiveWrappedActor with Receiving {

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

  private def tweet(record: ConsumerRecord[Array[Byte], Array[Byte]]): Tweet = {
    Tweet("some_text", Seq("redbee", "linux"), System.currentTimeMillis()) // TODO: deserialize tweet here!
  }

}
