package sma.track

import akka.actor.{ActorRef, Props}
import sma.eventsourcing.{EventSourcing, Particle}
import sma.track.TrackingActor.{Connecting, HashTrackingTerms}
import sma.twitter.Tweet

object TrackingActor {
  def props(): Props = Props(classOf[TrackingActor])

  case class Connecting(wsActor: ActorRef)

  case class HashTrackingTerms(hashTrackingTerms: String)

}

class TrackingActor extends Particle with EventSourcing {
  override def receive: Receive = {
    case Connecting(wsActor) =>
      logReceiving("is connecting ")
      context become connected(wsActor)
  }

  def connected(wsActor: ActorRef): Receive = {
    case hashTrackingTerms: HashTrackingTerms =>
      logReceiving(s"starts tracking with hash = ${hashTrackingTerms.hashTrackingTerms}")
      val tracker: ActorRef = system.actorOf(Tracker.props())
      tracker ! hashTrackingTerms
    case t: Tweet =>
      logReceiving("forwarding tweet to ws actor!")
      wsActor ! t
  }
}
