package sma

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.RouteConcatenation._
import sma.booting.{Networkers, Feeders}
import sma.feeding.Feed
import sma.http._
import sma.digging.Digger

object Main extends HttpApp with App with Commands with Queries with Networkers with Feeders {

  implicit val digger: ActorRef = system.actorOf(Digger.props(), "digger")

  implicit val profile: ActorRef = system.actorOf(Feed.props(), "profile")

  wakeupNetworkers

  wakeupFeeders

  startServer("localhost", 8080)   // This will start the server until the return key is pressed

  def routes = commandRoutes ~ queryRoutes
}
