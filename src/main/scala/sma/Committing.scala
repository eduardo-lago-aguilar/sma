package sma

import akka.kafka.ProducerSettings
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer


trait StringSerializableMessage {
  def key: String
  def serialize: String
}

trait Committing extends EventSourcing {
  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  def kafkaProducer = producerSettings.createKafkaProducer()

  def kafkaProducerRecord(message: StringSerializableMessage, topic: String): ProducerRecord[String, String] = new ProducerRecord[String, String](topic, message.key, message.serialize)
}
