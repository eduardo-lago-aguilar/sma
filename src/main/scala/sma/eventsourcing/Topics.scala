package sma.eventsourcing

import akka.actor.ActorSystem

trait Topics {
  implicit val system: ActorSystem = ActorSystem("sma")

  val networks = Seq("twitter")

  def digTopic(follower: String, network: String): String = {
    s"${follower}_at_${network}"
  }

  def replyTopic(topic: String) = s"${topic}_reply"

}
