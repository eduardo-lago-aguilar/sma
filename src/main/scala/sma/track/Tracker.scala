package sma.track

import akka.Done
import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import sma.eventsourcing.Receiving
import sma.reactive.ReactiveWrappedActor
import sma.twitter.Tweet

import scala.concurrent.Future
import scala.util.Random

object Tracker {
  def props(): Props = Props(classOf[Tracker])
}

class Tracker(topic: String, trackerActor: ActorRef) extends ReactiveWrappedActor with Receiving {
  override def receive: Receive = {
    case t: Tweet =>
      logReceiving("receiving tweet")
      trackerActor ! t
  }

  override def consume: Future[Done] = {
    val consumerGroup = s"${self.path.name}__tracker_${Random.nextInt()}"
    log.info(s"start consuming from ${topic}")
    plainSource(topic, consumerGroup)
      .mapAsync(1)(record => self ? decodeTweet(record))
      .runWith(Sink.ignore)
  }

}
