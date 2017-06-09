package sma.http

import akka.NotUsed
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.scaladsl.{Flow, Source}
import akka.util.Timeout
import sma.Settings
import sma.eventsourcing.{EventSourcing, User}
import sma.storing.Redis.{lrangeStream, smembersStream}
import sma.twitter.{TrackingTerm, Tweet, TweetJsonHelper}

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
          complete(board(hashTrackingTerms).map(json => Tweet(TweetJsonHelper.decodeId(json).get.toString, json, Seq(), timestamp, hashTrackingTerms)))
        }
    } ~ path("users") {
      get {
        complete(Source(Settings.theUsers).map(name => User(name)))
      }
    } ~ path("ws-echo") {
      get {
        handleWebSocketMessages(echoFlow)
      }
    }

  }

  def trackingTerms(userAtNetwork: String): Source[String, NotUsed] = smembersStream(digTopic(userAtNetwork))

  def board(hashTrackingTerms: String) = lrangeStream(hashTrackingTerms)

}
