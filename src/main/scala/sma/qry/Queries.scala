package sma.qry

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FutureDirectives.onSuccess
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import sma.EventSourcing
import sma.qry.QueryMessages.{InterestReply, Interests}

import scala.concurrent.duration._

trait Queries extends EventSourcing {

  implicit val profile: ActorRef

  val queryRoutes: Route = {

    implicit val timeout = Timeout(5 seconds)

    path("interests" / Segment) {
      interest =>
        get {
          onSuccess(profile ? Interests(interest)) {
            case reply: InterestReply =>
              reply.topics.foreach(topics => {
                topics.foreach(topic => println(s"--> [${reply.interest}] is interested in ${topic}"))
              })
              complete(StatusCodes.OK, reply.mkString)
            case _ =>
              complete(StatusCodes.InternalServerError)
          }
        }
    }
  }
}
