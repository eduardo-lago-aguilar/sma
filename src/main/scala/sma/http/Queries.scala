package sma.http

import akka.NotUsed
import akka.actor.PoisonPill
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import sma.Settings
import sma.eventsourcing.EventSourcing
import sma.http.SocketTrackingActor.Connecting
import sma.json.Json
import sma.profiling.User
import sma.storing.Redis.smembersStream
import sma.twitter.{HashTrackingTerms, TrackingTerm, Tweet}

import scala.concurrent.duration._

trait Queries extends EventSourcing {

  import sma.json.CustomJsonProtocol._

  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  val queryRoutes: Route = {
    implicit val timeout = Timeout(Settings.http.timeout seconds)

    val trackingTermsRoute: Route = path(Segment / "terms") {
      userAtNetwork =>
        get {
          complete(trackingTerms(userAtNetwork).map(TrackingTerm))
        }
    }

    val usersRoute: Route = path("users") {
      get {
        complete(getUsers)
      }
    }

    val tweetsRoute: Route = path(Segment / "tweets") {
      userAtNetwork =>
        get {
          handleWebSocketMessages(createTermsTrackingFlow())
        }
    }

    trackingTermsRoute ~ usersRoute ~ tweetsRoute
  }

  private def trackingTerms(userAtNetwork: String): Source[String, NotUsed] = smembersStream(digTopic(userAtNetwork))

  private def getUsers = Source(Settings.theUsers).map(name => User(name))

  private def createTermsTrackingFlow() = {
    val trackingWsActor = system.actorOf(SocketTrackingActor.props())

    val incomingTraffic: Sink[Message, NotUsed] = Flow[Message].map {
      case TextMessage.Strict(text) => HashTrackingTerms(hashTrackingTerms = text)
      case _ => None
    }.to(Sink.actorRef(trackingWsActor, PoisonPill))

    val outgoingTraffic: Source[Strict, NotUsed.type] = Source.actorRef[Tweet](1000, OverflowStrategy.fail)
      .mapMaterializedValue { outgoingActor =>
        trackingWsActor ! Connecting(outgoingActor)
        NotUsed
      }.map {
      case tweet: Tweet => TextMessage(Json.encodeAsString(tweet))
    }
    Flow.fromSinkAndSource(incomingTraffic, outgoingTraffic)
  }

}
