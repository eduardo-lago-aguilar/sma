package sma

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.RouteConcatenation._
import sma.booting.{ProfilesBoot, NetworkersBoot, FeedersBoot}
import sma.feeding.{Profiling, Profiling$}
import sma.http._
import sma.digging.Digger

object Main extends HttpApp with App with Commands with Queries with NetworkersBoot with FeedersBoot with ProfilesBoot {

  implicit val digger: ActorRef = system.actorOf(Digger.props(), "digger")

  implicit var profiles = Map[String, ActorRef]().empty

  wakeupNetworkers

  wakeupFeeders

  wakeupProfiles

  startServer("localhost", 8080)   // This will start the server until the return key is pressed

  def routes = commandRoutes ~ queryRoutes
}
