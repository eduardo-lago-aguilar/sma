package sma.reactive

import akka.actor.{ActorRef, ActorSystem, Props}

import scala.concurrent.duration._

object ReactiveStreamWrapper {
  def apply(implicit system: ActorSystem, childName: String, props: Props): ActorRef = {
    val name = s"supervisor_of_${childName}"

    println(s"--> [${name}] creating supervisor")

    import akka.pattern.{Backoff, BackoffSupervisor}

    val supervisorProps = BackoffSupervisor.props(
      Backoff.onStop(
        props,
        childName = childName,
        minBackoff = 3 seconds,
        maxBackoff = 30 seconds,
        randomFactor = 0.2
      )
    )
    system.actorOf(supervisorProps, name = name)
  }
}
