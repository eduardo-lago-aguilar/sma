package sma.reactive

import akka.Done
import sma.Settings
import sma.eventsourcing.{EventSourcing, Particle}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait ReactiveWrappedActor extends Particle with EventSourcing {
  implicit val timeout = akka.util.Timeout(Settings.reactive.timeout seconds)

  override def preStart: Unit = {
    logStarting
    reactive
  }

  def consume: Future[Done]

  protected def reactive: Unit = {
    log.info(s"--> [${self.path.name}] creating reactive stream")
    consume.onComplete {
      case Failure(ex) =>
        log.error(ex, s"--> [${self.path.name}] stream failed!")
        suicide
      case Success(_) =>
        log.info(s"--> [${self.path.name}] gracefully shutdown")
    }
  }
}
