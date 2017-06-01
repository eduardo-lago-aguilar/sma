package sma

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

trait EventSourcing {
  implicit val system: ActorSystem = ActorSystem("sma")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

}
