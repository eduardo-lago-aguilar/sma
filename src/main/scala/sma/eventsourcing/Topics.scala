package sma.eventsourcing

import akka.actor.ActorSystem
import sma.eventsourcing.Hash._

trait Topics {
  implicit val system: ActorSystem = ActorSystem("sma")

  val networks = Seq("twitter")

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

  def trackingTermsTopic(baseTopic: String, trackingTerms: Seq[String]): String = trackingTermsTopic(baseTopic, sha256(trackingTerms.toVector))
  def trackingTermsTopic(baseTopic: String, hashTrackingTerms: String): String = s"${baseTopic}_${hashTrackingTerms}"

}
