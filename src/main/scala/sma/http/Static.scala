package sma.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.PathDirectives._

trait Static {

  val staticRoutes: Route = {
    path("/index.html") {
      getFromFile("index.html")
    }

  }
}
