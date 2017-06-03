package sma.cmd

import akka.actor.{Actor, ActorLogging, Props}
import sma.cmd.DiggingMessages.Follow

object Facebook {
  def props(): Props = {
    Props(classOf[Facebook])
  }
}

class Facebook extends Actor with ActorLogging {
  override def receive = {
    case f: Follow =>
      log.info(s"@facebook following ${f.serialize}")
      sender() ! f.reply
  }
}
