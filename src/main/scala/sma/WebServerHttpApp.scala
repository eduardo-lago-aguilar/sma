package sma

import akka.http.scaladsl.server.HttpApp
import redis.RedisClient
import sma.cmd._
import sma.qry.{Profile, Queries}

object WebServerHttpApp extends HttpApp with App with Commands with Queries with Networkers{

  implicit val digger = system.actorOf(Digger.props(), "digger")

  implicit val profile = system.actorOf(Profile.props(), "profile")

  wakeupNetworkers

  startServer("localhost", 8080)   // This will start the server until the return key is pressed

  def routes = commandRoutes ~ queryRoutes

  def wakeupNetworkers: Unit = {
    val redis = RedisClient()
    val future = redis.smembers[String]("sma-users")
    future.foreach(users => {
      for (user <- users) {
        for ((net, streamWrapper) <- supervisors) {
          wakeup(user, net)
        }
      }
    })
  }
}
