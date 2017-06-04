package sma

import akka.kafka.ProducerSettings
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import sma.common.Json
import sma.twitter.{TrackTerms, Tweet}


trait StringSerializableMessage {
  def key: String

  def serialize: String
}

trait Committing extends EventSourcing {
  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServers)

  def kafkaProducer = producerSettings.createKafkaProducer()

  def kafkaProducerRecord(message: StringSerializableMessage, topic: String): ProducerRecord[String, String] = kafkaProducerRecord(topic, message.key, message.serialize)

  def kafkaProducerRecord(topic: String, key: String, value: String): ProducerRecord[String, String] = new ProducerRecord[String, String](topic, key, value)

  // twitter specific
  val twitterProducerSettings = ProducerSettings(system, new ByteArraySerializer, new ByteArraySerializer)
    .withBootstrapServers(bootstrapServers)

  def twitterKafkaProducer = twitterProducerSettings.createKafkaProducer()

  def twitterProducerRecord(tweet: Tweet, topic: String) = {
    val key = Json.ByteArray.encode(TrackTerms(tweet.trackTerms))
    val value = Json.ByteArray.encode(tweet)
    new ProducerRecord[Array[Byte], Array[Byte]](topic, key, value)
  }
}
