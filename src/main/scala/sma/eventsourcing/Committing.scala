package sma.eventsourcing

import akka.kafka.ProducerSettings
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer
import sma.Settings

trait Committing extends EventSourcing {
  type ProducerRecordType = ProducerRecord[Array[Byte], Array[Byte]]

  private val keySerializer: ByteArraySerializer = new ByteArraySerializer
  private val valueSerializer: ByteArraySerializer = new ByteArraySerializer

  val producerSettings = ProducerSettings(system, keySerializer, valueSerializer)
    .withBootstrapServers(Settings.kafka.bootstrapServers)

  def producer = producerSettings.createKafkaProducer()
}
