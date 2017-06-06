package sma.http

import akka.NotUsed
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model.ws.{TextMessage, Message}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.scaladsl.{Flow, Source}
import akka.util.Timeout
import sma.eventsourcing.EventSourcing
import sma.json.Json
import sma.storing.Redis.{MessagesStore, TrackingTermsStore}
import sma.twitter.{TrackingTerm, Tweet}

import scala.concurrent.duration._

trait Queries extends EventSourcing {

  import sma.json.CustomJsonProtocol._

  implicit val timeout = Timeout(5 seconds)

  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  val echoFlow: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(text) => TextMessage(s"I got your message: $text!")
    case _ => TextMessage(s"Sorry I didn't quite get that")
  }


  val queryRoutes: Route = {
    path(Segment / "terms") {
      userAtNetwork =>
        get {
          complete(trackingTerms(userAtNetwork).map(TrackingTerm))
        }
    } ~ path(Segment / "board" / Segment) {
      (userAtNetwork, hashTrackingTerms) =>
        get {
          complete(board(userAtNetwork, hashTrackingTerms).map(body => Tweet(Json.Tweet.decodeId(body), body, Seq(), timestamp)))
        }
    } ~ path("ws-echo") {
      get {
        handleWebSocketMessages(echoFlow)
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
