package sma.qry

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.PathMatcher._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FutureDirectives.onSuccess
import akka.http.scaladsl.server.directives.MethodDirectives.put
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import sma.EventSourcing
import sma.qry.QueryMessages.{TopicsReply, Topics}

import scala.concurrent.duration._

trait Queries extends EventSourcing {

  implicit val profile: ActorRef

  val queryRoutes: Route = {

    implicit val timeout = Timeout(5 seconds)

    path("topics") {
        put {
          onSuccess(profile ? Topics("user123")) {
            case reply: TopicsReply =>
              complete(StatusCodes.OK, reply.mkString)
            case _ =>
              complete(StatusCodes.InternalServerError)
          }
        }
    }
  }
}
