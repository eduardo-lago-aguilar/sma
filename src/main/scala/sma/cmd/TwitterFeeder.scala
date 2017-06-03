package sma.cmd

import akka.Done
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import sma.Receiving
import sma.reactive.ReactiveWrappedActor

import scala.concurrent.Future
import scala.concurrent.duration._

class TwitterFeeder(topic: String) extends ReactiveWrappedActor with Receiving{

  override def receive = {
    case tweets: Vector[Any] =>
      println(s"receiving transmission: ${tweets.length}")
      sender() ! Some()
  }

  override def consume: Future[Done] = consumeAndBatchAsync

  private def consumeAndBatchAsync: Future[Done] = {
    Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))
      .map(msg => msg.value())
      .groupedWithin(100, 2 seconds)
      .mapAsync(1)(bulk => self ? bulk)
      .runWith(Sink.ignore)
  }

}
