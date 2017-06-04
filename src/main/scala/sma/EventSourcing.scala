package sma

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

trait Topics {
  implicit val system: ActorSystem = ActorSystem("sma")

  val networks = Seq("twitter")

  def digTopic(follower: String, network: String): String = {
    s"${follower}_at_${network}"
  }

  def replyTopic(topic: String) = s"${topic}_reply"

  val bootstrapServers: String = "localhost:9092"
}

trait EventSourcing extends Topics {
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def timestamp: Long = System.currentTimeMillis()
}
