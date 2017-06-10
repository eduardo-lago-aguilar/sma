package sma.track

import akka.Done
import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import sma.eventsourcing.Receiving
import sma.reactive.ReactiveWrappedActor
import sma.twitter.{Tweet, TweetJsonHelper, TweetReply}

import scala.concurrent.Future

object QueryTracker {
  def props(): Props = Props(classOf[QueryTracker])
}

class QueryTracker(topic: String, trackerActor: ActorRef) extends ReactiveWrappedActor with Receiving {
  override def receive: Receive = {
    case tweet: Tweet =>
      sender() ! TweetReply()
      logReceiving(s"a tweet message w/ id = ${tweet.id}, forwarding message to TrackerActor")
      trackerActor ! tweet
  }

  override def consume: Future[Done] = {
    val consumerGroup = s"${self.path.name}__${java.util.UUID.randomUUID.toString}"
    plainSource(topic, consumerGroup)
      .mapAsync(1)(record => self ? TweetJsonHelper.decodeTweet(record))
      .runWith(Sink.ignore)
  }

}
