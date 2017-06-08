package sma.eventsourcing

import akka.actor.ActorSystem

trait Topics {
  implicit val system: ActorSystem = ActorSystem("sma")

  def digTopic(follower: String, network: String): String = {
    s"${follower}_at_${network}"
  }

  def digTopic(userAtNetwork: String): String = {
    val Array(user, network) = splittingUserAt(userAtNetwork)
    digTopic(user, network)
  }

  def replyTopic(topic: String) = s"${topic}_reply"

  def splittingUserAt(userAtNetwork: String) = {
    val user = userAtNetwork.split("@")(0)
    val network = userAtNetwork.split("@")(1)
    Array(user, network)
  }

  def splittingTermAt(termAtNetwork: String) = splittingUserAt(userAtNetwork = termAtNetwork)

}
