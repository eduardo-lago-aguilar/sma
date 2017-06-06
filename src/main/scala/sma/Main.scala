package sma

import akka.actor.ActorRef
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.RouteConcatenation._
import sma.booting.{FeedersBoot, NetworkersBoot, ProfilesBoot}
import sma.digging.Digger
import sma.http._

object Main extends HttpApp with App with Commands with Queries with NetworkersBoot with FeedersBoot with ProfilesBoot {

  implicit val digger: ActorRef = system.actorOf(Digger.props(), "digger")

  wakeupNetworkers

  wakeupFeeders

  wakeupProfiles

  startServer("localhost", 8080)

  def routes = commandRoutes ~ queryRoutes
}
