package sma.http

import akka.http.scaladsl.model.StatusCodes.MovedPermanently
import akka.http.scaladsl.server.Directives.{redirect, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FileAndResourceDirectives.getFromFile
import akka.http.scaladsl.server.directives.PathDirectives.{path, pathSingleSlash}
import sma.Settings.{defaultNetwork, defaultUser}

trait Static {

  val staticRoutes: Route = {
    pathSingleSlash {
      redirect(s"${defaultUser}@${defaultNetwork}", MovedPermanently)
    } ~ pathPrefix("root") {
      getFromDirectory("static/root/")
    } ~ path(Segment) {
      _ => {
        getFromFile("static/root/sma/index.html")
      }
    }
  }

}
