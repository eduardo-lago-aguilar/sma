package sma.http

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.FutureDirectives.onSuccess
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, put}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import sma.Settings
import sma.digging.{DiggerActor, Digging}
import sma.eventsourcing.EventSourcing

import scala.concurrent.duration._

trait Commands extends EventSourcing {

  def digger() = system.actorOf(Props(new DiggerActor), s"digger_${java.util.UUID.randomUUID.toString}")

  val commandRoutes = path(Segment / Segment) {
    implicit val timeout = Timeout(Settings.http.timeout seconds)

    (userAtNetwork, term) =>
      val Array(user, network) = splittingUserAt(userAtNetwork)

      val followRoute = put {
        onSuccess(digger() ? Digging(user, network, term, "follow")) {
          case _ => ok
        }
      }

      val forgetRoute = delete {
        onSuccess(digger() ? Digging(user, network, term, "forget")) {
          case _ => ok
        }
      }

      followRoute ~ forgetRoute
  }

  def ok = complete(StatusCodes.OK)
}
