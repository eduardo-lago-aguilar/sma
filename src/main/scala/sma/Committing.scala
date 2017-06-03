package sma

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{BytesSerializer, StringSerializer}
import org.apache.kafka.common.utils.Bytes


trait BytesSerializableMessage {
  def serialize: Bytes
}

trait Committing extends EventSourcing {
  val producerSettings = ProducerSettings(system, new StringSerializer, new BytesSerializer)
    .withBootstrapServers("localhost:9092")

  def plainSink = Producer.plainSink(producerSettings)

  def kafkaProducer = producerSettings.createKafkaProducer()

  def kafkaProducerRecord(message: BytesSerializableMessage, topic: String): ProducerRecord[String, Bytes] = new ProducerRecord[String, Bytes](topic, message.serialize)
}
