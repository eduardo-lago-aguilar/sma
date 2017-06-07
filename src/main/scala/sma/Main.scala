package sma

import akka.actor.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.{IncomingConnection, ServerBinding}
import akka.http.scaladsl.server.RouteConcatenation._
import akka.stream.scaladsl.{Sink, Source}
import sma.booting.{FeedersBoot, NetworkersBoot, ProfilesBoot}
import sma.digging.Digger
import sma.http._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.sys.ShutdownHookThread

object Main extends App with Commands with Queries with Static with NetworkersBoot with FeedersBoot with ProfilesBoot {

  implicit val digger: ActorRef = system.actorOf(Digger.props(), "digger")

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

    val HOST = "127.0.0.1"
    val PORT = 8080

    val serverSource: Source[IncomingConnection, Future[ServerBinding]] =
      httpService.bind(interface = HOST, PORT)

    val binding: Future[ServerBinding] =
      serverSource.to(Sink.foreach { connection =>
        connection handleWith routes
      }).run()

    println(s"Server is now online at http://$HOST:$PORT\n")

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
