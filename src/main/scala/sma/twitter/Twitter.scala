package sma.twitter

import akka.kafka.{Subscriptions, ConsumerSettings}
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.Control
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import sma.Receiving
import sma.common.Json

trait Twitter extends Receiving {
  type TweetConsumerRecord = ConsumerRecord[Array[Byte], Array[Byte]]

  private val keyDeserializer: ByteArrayDeserializer = new ByteArrayDeserializer
  private val valueDeserializer: ByteArrayDeserializer = new ByteArrayDeserializer
  private val consumerGroup: String = "group2"
  private val AUTO_OFFSET_RESET_CONFIG: String = "earliest"

  val twitterConsumerSettings = ConsumerSettings(system, keyDeserializer, valueDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withGroupId(consumerGroup)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_CONFIG)

  def twitterConsumer = twitterConsumerSettings.createKafkaConsumer()

  def twitterPlainSource(topic: String): Source[TweetConsumerRecord, Control] = {
    Consumer.plainSource(twitterConsumerSettings, Subscriptions.topics(topic))
  }

  def tweet(record: TweetConsumerRecord): Tweet = {
    Json.ByteArray.decode[Tweet](record.value())
  }

}
