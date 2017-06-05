package sma.reactive

import akka.Done
import sma.eventsourcing.{EventSourcing, Particle}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait ReactiveWrappedActor extends Particle with EventSourcing {
  implicit val timeout = akka.util.Timeout(5 seconds)
  val batchPeriod = 2 seconds
  val batchSize = 1000

  starting

  override def preStart: Unit = makeItReactive

  def makeItReactive: Unit = {
    log.info(s"--> [${self.path.name}] creating reactive stream")
    consume.onComplete {
      case Failure(ex) =>
        log.error(ex, s"--> [${self.path.name}] stream failed, stopping the actor")
        suicide
      case Success(_) =>
        log.info(s"--> [${self.path.name}] gracefully shutdown")
    }
  }

  def consume: Future[Done]
}
