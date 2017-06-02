package sma.cmd

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.utils.Bytes
import sma.Committing
import sma.cmd.DiggingMessages._


object Digger {
  def props(): Props = {
    Props(classOf[Digger])
  }
}

class Digger(twitter: ActorRef, facebook: ActorRef) extends Actor with ActorLogging with Committing {

  val medias = Map("twitter" -> twitter, "facebook" -> facebook)

  override def receive = {
    case f: Digging =>
      forward(f, f.media)

      log.info(s"received ${f.mkString}")
      sender() ! f.reply
  }

  def forward(message: Digging, media: String) = {
    kafkaProducer.send(kafkaRecord(message, "topic7"))
    medias(media) ! message
  }

  def kafkaRecord(message: Digging, topic: String): ProducerRecord[String, Bytes] = new ProducerRecord[String, Bytes](topic, message.serialize)
}
