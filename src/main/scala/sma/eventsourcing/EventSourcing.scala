package sma.eventsourcing

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

trait EventSourcing  {
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
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val bootstrapServers: String = "localhost:9092"

  implicit var closingApp = false

}
