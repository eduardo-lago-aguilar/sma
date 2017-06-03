package sma.reactive

import akka.Done
import akka.actor.{Actor, PoisonPill, ActorLogging}
import sma.EventSourcing

import scala.concurrent.Future
import scala.util.{Success, Failure}
import scala.concurrent.duration._

trait ReactiveWrappedActor extends Actor with ActorLogging with EventSourcing {
  implicit val timeout = akka.util.Timeout(5 seconds)
  val batchPeriod = 2 seconds
  val batchSize = 1000

  println(s"--> [${self.path.name}] creating actor ")

  override def preStart: Unit = makeItReactive
  override def postStop(): Unit = println(s"--> [${self.path.name}] actor stopped")
  override def unhandled(message: Any): Unit = {
    println(s"--> [${self.path.name}] receiving an unknown message")
    self ! PoisonPill
  }

  def makeItReactive: Unit = {
    log.info(s"--> [${self.path.name}] creating reactive stream")
    consume.onComplete {
      case Failure(ex) =>
        log.error(ex, s"--> [${self.path.name}] stream failed, stopping the actor")
        self ! PoisonPill
      case Success(_) =>
        log.info(s"--> [${self.path.name}] gracefully shutdown")
    }
  }

  def consume: Future[Done]
}
