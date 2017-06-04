package sma.digging

import akka.actor.{Actor, ActorLogging, Props}
import org.apache.kafka.clients.producer.ProducerRecord
import sma.json.Json
import sma.eventsourcing.Committing

object Digger {
  def props(): Props = {
    Props(classOf[Digger])
  }
}

class Digger extends Actor with ActorLogging with Committing {

  override def receive = {
    case message: Digging =>
      commit(message, digTopic(message.follower, message.network))

      log.info(s"--> [${self.path.name}] received ${message.mkString}")

      sender() ! DiggingReply()
  }

  def commit(message: Digging, topic: String) = {
    producer.send(diggingProducerRecord(message, topic))
  }

  private def diggingProducerRecord(dig: Digging, topic: String) = {
    val key = Json.ByteArray.encode(dig.key)
    val value = Json.ByteArray.encode(dig)
    new ProducerRecord[Array[Byte], Array[Byte]](topic, key, value)
  }


}
