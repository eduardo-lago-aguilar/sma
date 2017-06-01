package sma

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.PathMatcher._
import akka.http.scaladsl.server.PathMatchers._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FutureDirectives.onSuccess
import akka.http.scaladsl.server.directives.MethodDirectives.put
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import sma.DiggingMessages.{Follow, FollowReply}

import scala.concurrent.duration._


trait Commands extends EventSourcing {

  implicit val digger: ActorRef

  val commandRoutes: Route = {

    implicit val timeout = Timeout(5 seconds)

    path("boards" / Segment) {
      interest =>
        put {
          onSuccess(digger ? Follow("user123", interest)) {
            case response: FollowReply =>
              complete(StatusCodes.OK, s"follow ack received!")
            case _ =>
              complete(StatusCodes.InternalServerError)
          }
        }
    }
  }
}
