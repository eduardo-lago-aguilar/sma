package sma.track

import akka.actor.Props
import sma.eventsourcing.{EventSourcing, Particle}
import sma.track.TrackingActor.HashTrackingTerms
import sma.twitter.Tweet

object Tracker {
  def props(): Props = Props(classOf[Tracker])
}

class Tracker extends Particle with EventSourcing {
  override def receive: Receive = {
    case HashTrackingTerms(hashTrackingTerms) =>
      logReceiving("tracking request")
      sender() ! Tweet(id = hashTrackingTerms, body = hashTrackingTerms, Seq(hashTrackingTerms), timestamp = timestamp, hashTrackingTerms = hashTrackingTerms)
  }
}
