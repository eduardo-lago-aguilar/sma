package sma.eventsourcing

import akka.actor.{Actor, ActorLogging, PoisonPill}

trait Particle extends Actor with ActorLogging {

  def logStarting = log.info(s"--> [${self.path.name}] actor is starting")

  def logStopped = log.info(s"--> [${self.path.name}] actor stopped")

  def logReceiving(text: String) = log.info(s"--> [${self.path.name}] actor received ${text}")

  def logUnknown = log.error(s"--> [${self.path.name}] actor received an unknown message")

  def suicide = self ! PoisonPill

  override def unhandled(message: Any): Unit = {
    logUnknown
    suicide
  }

  override def postStop(): Unit = logStopped

}
