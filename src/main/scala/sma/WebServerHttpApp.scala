package sma

import akka.http.scaladsl.server.HttpApp

object WebServerHttpApp extends HttpApp with App with Commands {

  implicit val digger = system.actorOf(Digger.props(), "digger")

  def routes = commandRoutes

  // This will start the server until the return key is pressed
  startServer("localhost", 8080)
}
