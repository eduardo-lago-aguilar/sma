package sma.eventsourcing

import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.Control
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.ByteArrayDeserializer

trait Receiving extends EventSourcing {
  type ConsumerRecordType = ConsumerRecord[Array[Byte], Array[Byte]]

  private val keyDeserializer: ByteArrayDeserializer = new ByteArrayDeserializer
  private val valueDeserializer: ByteArrayDeserializer = new ByteArrayDeserializer
  private val AUTO_OFFSET_RESET_CONFIG: String = "earliest"

  private def consumerSettings(group: String) =
    ConsumerSettings(system, keyDeserializer, valueDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId(group)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_CONFIG)

  def plainSource(topic: String, group: String): Source[ConsumerRecordType, Control] = {
    Consumer.plainSource(consumerSettings(group), Subscriptions.topics(topic))
  }

}
