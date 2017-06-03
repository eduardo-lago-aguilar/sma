package sma

import akka.http.scaladsl.server.HttpApp
import sma.Redis.smaUsers
import sma.cmd._
import sma.qry.{Profile, Queries}

object WebServerHttpApp extends HttpApp with App with Commands with Queries with Networkers{

  implicit val digger = system.actorOf(Digger.props(), "digger")

  implicit val profile = system.actorOf(Profile.props(), "profile")

  wakeupNetworkers

  startServer("localhost", 8080)   // This will start the server until the return key is pressed

  def routes = commandRoutes ~ queryRoutes
}
