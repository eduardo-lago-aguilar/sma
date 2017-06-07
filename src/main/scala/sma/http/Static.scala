package sma.http

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FileAndResourceDirectives.getFromFile
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.Directives.redirect
import akka.http.scaladsl.server.directives.PathDirectives.pathSingleSlash
import akka.http.scaladsl.model.StatusCodes.MovedPermanently
import akka.http.scaladsl.server.Directives._
import sma.Settings.{firstNet, firstUser}

trait Static {

  val staticRoutes: Route = {
    pathSingleSlash {
      redirect(s"${firstUser}@${firstNet}", MovedPermanently)
    } ~ path("sma.js") {
      getFromFile("static/sma.js")
    } ~ path("home.html") {
      getFromFile("static/home.html")
    } ~ pathPrefix("lib") {
      getFromDirectory("static/lib/")
    } ~ path(Segment) {
      _ => {
        getFromFile("static/index.html")
      }
    }
  }

}
