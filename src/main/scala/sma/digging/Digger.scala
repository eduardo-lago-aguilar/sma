package sma.digging

import akka.actor.Props
import org.apache.kafka.clients.producer.ProducerRecord
import sma.eventsourcing.{Committing, Particle}
import sma.json.Json

object Digger {
  def props(): Props = {
    Props(classOf[Digger])
  }
}

class Digger extends Particle with Committing {

  override def receive = {
    case message: Digging =>
      commit(message, digTopic(message.user, message.network))

      receiving(message.mkString)

      sender() ! DiggingReply()
  }

  def commit(message: Digging, topic: String) = {
    producer.send(diggingProducerRecord(message, topic))
  }

  private def diggingProducerRecord(dig: Digging, topic: String) = {
    val key = Json.encode(dig.key)
    val value = Json.encode(dig)
    new ProducerRecord[Array[Byte], Array[Byte]](topic, key, value)
  }

}
