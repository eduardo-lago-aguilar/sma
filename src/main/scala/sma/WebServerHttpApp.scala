package sma

import akka.actor.Props
import akka.http.scaladsl.server.HttpApp
import sma.cmd.{Twitter, Facebook, Digger, Commands}
import sma.qry.{Profile, Queries}

object WebServerHttpApp extends HttpApp with App with Commands with Queries {

  implicit val twitter = system.actorOf(Twitter.props(), "twitter")
  implicit val facebook = system.actorOf(Facebook.props(), "facebook")
  implicit val digger = system.actorOf(Props(new Digger(twitter, facebook)), "digger")

  implicit val profile = system.actorOf(Profile.props(), "profile")

  def routes = commandRoutes ~ queryRoutes

  // This will start the server until the return key is pressed
  startServer("localhost", 8080)
}
