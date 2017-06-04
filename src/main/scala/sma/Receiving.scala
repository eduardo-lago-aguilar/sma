package sma

import akka.kafka.{Subscriptions, ConsumerSettings}
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.Control
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerConfig}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

trait Receiving extends EventSourcing {
  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers )
    .withGroupId("group1")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  def kafkaConsumer = consumerSettings.createKafkaConsumer()


  // twitter

  val twitterConsumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withGroupId("group2")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  def twitterKafkaConsumer = twitterConsumerSettings.createKafkaConsumer()

  def twitterPlainSource(topic: String): Source[ConsumerRecord[Array[Byte], Array[Byte]], Control] = {
    Consumer.plainSource(twitterConsumerSettings, Subscriptions.topics(topic))
  }

}
