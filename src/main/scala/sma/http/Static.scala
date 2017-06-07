package sma.http

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FileAndResourceDirectives.getFromFile
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.Directives.redirect
import akka.http.scaladsl.server.directives.PathDirectives.pathSingleSlash
import akka.http.scaladsl.model.StatusCodes.MovedPermanently
import akka.http.scaladsl.server.Directives._

trait Static {

  val staticRoutes: Route = {
    pathSingleSlash {
      redirect(s"${sma.Settings.theUsers(0)}@${sma.Settings.networks(0)}", MovedPermanently)
    } ~ path(Segment) {
      _ => {
        getFromFile("index.html")
      }
    }
  }

}
