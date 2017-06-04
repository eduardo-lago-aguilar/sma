package sma

import akka.kafka.ProducerSettings
import org.apache.kafka.common.serialization.ByteArraySerializer

trait Committing extends EventSourcing {
  private val keySerializer: ByteArraySerializer = new ByteArraySerializer
  private val valueSerializer: ByteArraySerializer = new ByteArraySerializer

  val producerSettings = ProducerSettings(system, keySerializer, valueSerializer)
    .withBootstrapServers(bootstrapServers)

  val kafkaProducer = producerSettings.createKafkaProducer()
}
