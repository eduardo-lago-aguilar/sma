package sma.cmd

import akka.actor.{Actor, ActorLogging, Props}
import sma.Committing
import sma.msg.{DiggingReply, Digging}


object Digger {
  def props(): Props = {
    Props(classOf[Digger])
  }
}

class Digger extends Actor with ActorLogging with Committing {

  override def receive = {
    case message: Digging =>
      commit(message, message.digTopic)

      log.info(s"--> [${self.path.name}] received ${message.mkString}")

      sender() ! DiggingReply()
  }

  def commit(message: Digging, topic: String) = {
    kafkaProducer.send(diggingProducerRecord(message, topic))
  }

}
