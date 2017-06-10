package sma

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation._
import akka.stream.scaladsl.Sink
import sma.booting.{FeedersBoot, NetworkersBoot}
import sma.http._
import sma.profiling.ProfilesBoot

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.sys.ShutdownHookThread

object Main extends App with Commands with Queries with Static with NetworkersBoot with FeedersBoot with ProfilesBoot {

  if (Settings.wakeupNetworkers) {
    wakeupNetworkers
  }

  if (Settings.wakeupFeeders) {
    wakeupFeeders
  }

  if (Settings.wakeupProfilers) {
    wakeupProfiles
  }

  boot

  def routes = commandRoutes ~ queryRoutes ~ staticRoutes

  def boot: ShutdownHookThread = {
    val httpService = Http()

    val serverSource = httpService.bind(interface = Settings.http.host, Settings.http.port)

    val binding = serverSource.to(Sink.foreach { connection =>
      connection handleWith routes
    }).run()

    println(s"Server is now online at http://${Settings.http.host}:${Settings.http.port}\n")

    //shutdown Hook

    scala.sys.addShutdownHook {
      println("Terminating...")
      closingApp = true
      binding
        .flatMap(_.unbind())
        .onComplete { _ =>
          materializer.shutdown()
          system.terminate()
        }
      Await.result(system.whenTerminated, 5 seconds)
      println("Terminated... Bye")
    }
  }

}
