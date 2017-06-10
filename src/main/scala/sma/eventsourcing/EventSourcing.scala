package sma.eventsourcing

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

trait EventSourcing {

  implicit val system = ActorSystem("sma")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit var closingApp = false

  val bootstrapServers = "localhost:9092"

  def digTopic(follower: String, network: String): String = s"${follower}_at_${network}"

  def digTopic(userAtNetwork: String): String = {
    val Array(user, network) = splittingUserAt(userAtNetwork)
    digTopic(user, network)
  }

  def replyTopic(topic: String) = s"${topic}_reply"

  def splittingUserAt(userAtNetwork: String) = Array(userAtNetwork.split("@")(0), userAtNetwork.split("@")(1))

}
