package sma

import akka.actor.Props
import akka.http.scaladsl.server.HttpApp

object WebServerHttpApp extends HttpApp with App with Commands {

  implicit val twitter = system.actorOf(Twitter.props(), "twitter")
  implicit val facebook = system.actorOf(Facebook.props(), "facebook")
  implicit val digger = system.actorOf(Props(new Digger(twitter, facebook)), "digger")

  def routes = commandRoutes

  // This will start the server until the return key is pressed
  startServer("localhost", 8080)
}
