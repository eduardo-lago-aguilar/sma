package sma.track

import akka.actor.{ActorRef, Props}
import sma.eventsourcing.{EventSourcing, Particle}
import sma.reactive.ReactiveStreamWrapper
import sma.track.TrackingActor.{Connecting, HashTrackingTerms}
import sma.twitter.Tweet

import scala.util.Random

object TrackingActor {
  def props(): Props = Props(classOf[TrackingActor])

  case class Connecting(wsActor: ActorRef)

  case class HashTrackingTerms(hashTrackingTerms: String)

}

class TrackingActor extends Particle with EventSourcing {
  override def receive: Receive = {
    case Connecting(wsActor) =>
      logReceiving("is connecting")
      context become connected(wsActor)
  }

  def connected(wsActor: ActorRef): Receive = {
    case HashTrackingTerms(htt) =>
      logReceiving(s"starts tracking with hash = ${htt}")
      ReactiveStreamWrapper(system, s"tracker_${htt}_${Random.nextInt()}", Props(new Tracker(htt, self)))
    case t: Tweet =>
      logReceiving("forwarding tweet to ws actor!")
      wsActor ! t
  }
}
