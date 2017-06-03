package sma.cmd

import akka.Done
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import sma.{TweetReply, Tweet, Receiving}
import sma.reactive.ReactiveWrappedActor

import scala.concurrent.Future
import scala.concurrent.duration._

class TwitterFeeder(topic: String) extends ReactiveWrappedActor with Receiving {

  override def receive = {
    case tweet: Tweet =>
      println(s"receiving tweet: ${tweet.serialize}")
      sender() ! TweetReply()
  }

  override def consume: Future[Done] = consumeLinearAsync


  private def consumeLinearAsync: Future[Done] = {
    Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))
      .mapAsync(1)(m => self ? Tweet(m.value()))
      .runWith(Sink.ignore)
  }

  private def consumeAndBatchAsync: Future[Done] = {
    Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))
      .map(msg => msg.value())
      .groupedWithin(100, 2 seconds)
      .mapAsync(1)(bulk => self ? bulk)
      .runWith(Sink.ignore)
  }

}
