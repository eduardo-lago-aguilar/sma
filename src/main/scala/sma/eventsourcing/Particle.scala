package sma.eventsourcing

import akka.actor.{PoisonPill, ActorLogging, Actor}

trait Particle extends Actor with ActorLogging {

  def starting = log.info(s"--> [${self.path.name}] actor is starting")

  def stopped = log.info(s"--> [${self.path.name}] actor stopped")

  def receiving(text: String) = log.info(s"--> [${self.path.name}] actor received ${text}")

  def unknown = log.error(s"--> [${self.path.name}] actor received an unknown message")

  def suicide = self ! PoisonPill

  override def unhandled(message: Any): Unit = {
    unknown
    suicide
  }

  override def postStop(): Unit = stopped

}
