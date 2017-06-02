package sma.cmd

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
import sma.EventSourcing
import sma.cmd.DiggingMessages.{Follow, FollowReply, Forget, ForgetReply}

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
        } ~ delete {
          onSuccess(digger ? Forget("user123", interest)) {
            case response: ForgetReply =>
              complete(StatusCodes.OK, s"forget ack received!")
            case _ =>
              complete(StatusCodes.InternalServerError)
          }
        }
    }
  }
}
