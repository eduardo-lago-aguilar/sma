package sma.cmd

import akka.actor.{Actor, ActorLogging, Props}
import sma.cmd.DiggingMessages.Follow

object Twitter {
  def props(): Props = {
    Props(classOf[Twitter])
  }
}

class Twitter extends Actor with ActorLogging {
  override def receive = {
    case f: Follow =>
      log.info(s"@twitter following ${f.mkString}")
      sender() ! f.reply
  }
}
