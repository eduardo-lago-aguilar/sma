package sma

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.RouteConcatenation._
import sma.cmd._
import sma.qry.{Profile, Queries}

object Main extends HttpApp with App with Commands with Queries with Networkers{

  implicit val digger: ActorRef = system.actorOf(Digger.props(), "digger")

  implicit val profile: ActorRef = system.actorOf(Profile.props(), "profile")

  wakeupNetworkers

  startServer("localhost", 8080)   // This will start the server until the return key is pressed

  def routes = commandRoutes ~ queryRoutes
}
