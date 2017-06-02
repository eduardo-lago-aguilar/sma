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
    case dig: Digging =>
      commit(dig)
      forward(dig)

      log.info(s"received ${dig.mkString}")

      sender() ! dig.reply
  }

  def commit(message: Digging) = {
    kafkaProducer.send(kafkaRecord(message, digTopic(message.follower, message.media)))
  }

  def forward(message: Digging) = {
    medias(message.media) ! message
  }


}
