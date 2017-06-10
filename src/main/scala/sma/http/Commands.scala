package sma.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.FutureDirectives.onSuccess
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, put}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import sma.digging.{DiggerActor, Digging, DiggingReply}
import sma.eventsourcing.EventSourcing

import scala.concurrent.duration._

trait Commands extends EventSourcing {

  def digger() = system.actorOf(DiggerActor.props(), "digger")

  val commandRoutes = path(Segment / Segment) {
    implicit val timeout = Timeout(5 seconds)

    (userAtNetwork, term) =>
      val Array(user, network) = splittingUserAt(userAtNetwork)

      val followRoute = put {
        onSuccess(digger() ? Digging(user, network, term, "follow")) {
          case DiggingReply => ok
          case _ => error
        }
      }

      val forgetRoute = delete {
        onSuccess(digger() ? Digging(user, network, term, "forget")) {
          case DiggingReply => ok
          case _ => error
        }
      }

      followRoute ~ forgetRoute
  }

  def error = complete(StatusCodes.InternalServerError)

  def ok = complete(StatusCodes.OK)
}
