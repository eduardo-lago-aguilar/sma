package sma

import akka.kafka.ProducerSettings
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}


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
    // TODO: replace it by serializers
//    val key = Json.ByteArray.encode(TweetTrackTerms(tweet.trackTerms))
//    val value = Json.ByteArray.encode(tweet)
    val key = Array[Byte]()
    val value = Array[Byte]()
    new ProducerRecord[Array[Byte], Array[Byte]](topic, key, value)
  }
}
