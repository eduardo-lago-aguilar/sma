package sma.reactive

import akka.actor.{ActorRef, Props}
import sma.Settings
import sma.eventsourcing.EventSourcing

import scala.concurrent.duration._

object ReactiveStreamWrapper extends EventSourcing {

  def apply(childName: String, props: Props): ActorRef = {
    val name = s"supervisor_of_${childName}"

    import akka.pattern.{Backoff, BackoffSupervisor}

    val supervisorProps = BackoffSupervisor.props(
      Backoff.onStop(
        props,
        childName = childName,
        minBackoff = Settings.reactive.minBackoff seconds,
        maxBackoff = Settings.reactive.maxBackoff seconds,
        randomFactor = Settings.reactive.randomFator
      )
    )
    system.actorOf(supervisorProps, name = name)
  }
}
