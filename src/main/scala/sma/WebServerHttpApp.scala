package sma

import akka.http.scaladsl.server.HttpApp
import sma.cmd._
import sma.qry.{Profile, Queries}

object WebServerHttpApp extends HttpApp with App with Commands with Queries {

  implicit val digger = system.actorOf(Digger.props(), "digger")

  implicit val profile = system.actorOf(Profile.props(), "profile")

  def routes = commandRoutes ~ queryRoutes

  // This will start the server until the return key is pressed
  startServer("localhost", 8080)
}
