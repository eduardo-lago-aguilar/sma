package sma

import akka.http.scaladsl.server.HttpApp

import com.typesafe.config.ConfigFactory

/**
 * Server will be started calling `WebServerHttpApp.startServer("localhost", 8080)`
 * and it will be shutdown after pressing return.
 */
object WebServerHttpApp extends HttpApp with App with Commands {
  // Routes that this WebServer must handle are defined here
  // Please note this method was named `route` in versions prior to 10.0.7
  def routes = commandRoutes

  // This will start the server until the return key is pressed
  startServer("localhost", 8080)
}
