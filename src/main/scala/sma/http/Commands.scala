package sma.http

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FutureDirectives.onSuccess
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, put}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import sma.digging.{DiggingReply, Digging}
import sma.eventsourcing.EventSourcing

import scala.concurrent.duration._


trait Commands extends EventSourcing {

  implicit val digger: ActorRef

  val commandRoutes: Route = {

    implicit val timeout = Timeout(5 seconds)

    path(Segment / Segment) {
      (userAtNetwork, term) =>
        val Array(user, network) = splittingUserAt(userAtNetwork)
        put {
          onSuccess(digger ? Digging(user, network, term, "follow")) {
            case response: DiggingReply =>
              complete(StatusCodes.OK, s"follow ack received!")
            case _ =>
              complete(StatusCodes.InternalServerError)
          }
        } ~ delete {
          onSuccess(digger ? Digging(user, network, term, "forget")) {
            case response: DiggingReply =>
              complete(StatusCodes.OK, s"forget ack received!")
            case _ =>
              complete(StatusCodes.InternalServerError)
          }
        }
    }
  }
}
