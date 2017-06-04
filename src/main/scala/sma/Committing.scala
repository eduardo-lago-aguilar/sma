package sma

import akka.kafka.ProducerSettings
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import sma.common.Json
import sma.msg.Digging
import sma.twitter.{TrackTerms, Tweet}

trait Committing extends EventSourcing {
  private val keySerializer: ByteArraySerializer = new ByteArraySerializer
  private val valueSerializer: ByteArraySerializer = new ByteArraySerializer

  val producerSettings = ProducerSettings(system, keySerializer, valueSerializer)
    .withBootstrapServers(bootstrapServers)

  val kafkaProducer = producerSettings.createKafkaProducer()

  def diggingProducerRecord(dig: Digging, topic: String) = {
    val key = Json.ByteArray.encode(dig.key)
    val value = Json.ByteArray.encode(dig)
    new ProducerRecord[Array[Byte], Array[Byte]](topic, key, value)
  }

}
