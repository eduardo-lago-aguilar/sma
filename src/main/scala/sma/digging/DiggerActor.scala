package sma.digging

import akka.actor.Props
import org.apache.kafka.clients.producer.ProducerRecord
import sma.eventsourcing.{Committing, Particle}
import sma.json.Json

object DiggerActor {
  def props(): Props = Props(classOf[DiggerActor])
}

class DiggerActor extends Particle with Committing {

  override def receive = {
    case digging: Digging =>
      sender() ! DiggingReply()
      logReceiving(digging.mkString)
      commit(digging, digTopic(digging.user, digging.network))
  }

  def commit(message: Digging, topic: String) = producer.send(diggingProducerRecord(message, topic))

  private def diggingProducerRecord(dig: Digging, topic: String) = {
    val key = Json.encode(dig.key)
    val value = Json.encode(dig)
    new ProducerRecord[Array[Byte], Array[Byte]](topic, key, value)
  }

}
