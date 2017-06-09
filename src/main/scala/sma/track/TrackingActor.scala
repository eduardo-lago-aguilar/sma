package sma.track

import akka.actor.{ActorRef, Props}
import sma.eventsourcing.{EventSourcing, Particle}
import sma.reactive.ReactiveStreamWrapper
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
      log.info("Tracking actor is connecting....")
      context become connected(wsActor)
  }

  def connected(wsActor: ActorRef): Receive = {
    case HashTrackingTerms(hashTrackingTerms) =>
      log.info(s"Tracking actor is starting a new QueryTracker with hash = ${hashTrackingTerms}")
      val name = s"query_tracker_${hashTrackingTerms}_${java.util.UUID.randomUUID.toString}"
      ReactiveStreamWrapper(system, name, Props(new QueryTracker(hashTrackingTerms, self)))
    case tweet: Tweet =>
      log.info(s"Tracking actor received a tweet from Tracker with id = ${tweet.id}, tweet is being forwarded to websocket")
      wsActor ! tweet
  }
}
