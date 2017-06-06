package sma.http

import akka.NotUsed
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.scaladsl.Source
import akka.util.Timeout
import sma.eventsourcing.EventSourcing
import sma.storing.Redis.{MessagesStore, TrackingTermsStore}
import sma.twitter.{TrackingTerm, Tweet}

import scala.concurrent.duration._

trait Queries extends EventSourcing {

  import sma.json.CustomJsonProtocol._

  implicit val timeout = Timeout(5 seconds)

  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  val queryRoutes: Route = {
    path(Segment / "terms") {
      userAtNetwork =>
        get {
          complete(trackingTerms(userAtNetwork).map(TrackingTerm))
        }
    } ~ path(Segment / "board" / Segment) {
      (userAtNetwork, hashTrackingTerms) =>
        get {
          complete(board(userAtNetwork, hashTrackingTerms).map(s => Tweet(s, Seq(), timestamp)))
        }
    }

  }

  def trackingTerms(userAtNetwork: String): Source[String, NotUsed] = {
    Source.fromFuture(TrackingTermsStore(digTopic(userAtNetwork))).mapConcat(seq => seq.toStream)
  }

  def board(userAtNetwork: String, hashTrackingTopics: String) = {
    val ttt = trackingTermsTopic(replyTopic(digTopic(userAtNetwork)), hashTrackingTopics)
    Source.fromFuture(MessagesStore(ttt)).mapConcat(seq => seq.toStream)
  }
}
