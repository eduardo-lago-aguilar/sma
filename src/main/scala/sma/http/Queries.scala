package sma.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import sma.eventsourcing.{EventSourcing, ProfileActors}
import sma.feeding.RetrieveTrackingTerms
import sma.storing.Redis.TrackingTermsStore

import scala.concurrent.Await
import scala.concurrent.duration._

trait Queries extends EventSourcing with ProfileActors {

  val queryRoutes: Route = {

    implicit val timeout = Timeout(5 seconds)

    path("interests" / Segment) {
      userAtNetwork =>
        get {
          complete(StatusCodes.OK, trackingTerms(timeout, userAtNetwork).mkString(", "))
        }
    } ~ path("board" / Segment) {
      userAtNetwork =>
        get {
          val terms = trackingTerms(timeout, userAtNetwork)
          val ttt = trackingTermsTopic(replyTopic(digTopic(userAtNetwork)), terms)
          complete(StatusCodes.OK, terms.mkString(", "))
        }
    }

  }

  def trackingTerms(timeout: Timeout, userAtNetwork: String): Seq[String] = Await.result(TrackingTermsStore(digTopic(userAtNetwork)), timeout.duration)
}
