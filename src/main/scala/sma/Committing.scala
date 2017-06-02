package sma

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import org.apache.kafka.common.serialization.{BytesSerializer, StringSerializer}

trait Committing extends EventSourcing {
  val producerSettings = ProducerSettings(system, new StringSerializer, new BytesSerializer)
    .withBootstrapServers("localhost:9092")

  def plainSink = Producer.plainSink(producerSettings)

  def kafkaProducer = producerSettings.createKafkaProducer()
}
