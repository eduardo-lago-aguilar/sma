package sma.cmd

import akka.actor.{Actor, ActorLogging, Props}
import sma.Committing
import sma.cmd.DiggingMessages._


object Digger {
  def props(): Props = {
    Props(classOf[Digger])
  }
}

class Digger extends Actor with ActorLogging with Committing {

  override def receive = {
    case message: Digging =>
      val topic = digTopic(message.follower, message.media)
      commit(message, topic)
      wakeup(topic)

      log.info(s"--> [${self.path.name}] received ${message.mkString}")

      sender() ! message.reply
  }

  def commit(message: Digging, topic: String) = {
    kafkaProducer.send(kafkaRecord(message, topic))
  }

  def wakeup(topic: String) = StreamWrapperTwitter.create(system, topic)
}
