package sma

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

trait Topics {
  implicit val system: ActorSystem = ActorSystem("sma")

  def digTopic(follower: String, network: String): String = {
    s"${follower}_at_${network}"
  }

  def replyTopic(topic: String) = s"${topic}_reply"
}

trait EventSourcing extends Topics {
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher


}
