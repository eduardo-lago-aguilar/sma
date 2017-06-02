package sma

import akka.kafka.ConsumerSettings
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{BytesDeserializer, StringDeserializer}

trait Receiving extends EventSourcing {
  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new BytesDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("group1")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  def kafkaConsumer = consumerSettings.createKafkaConsumer()
}
