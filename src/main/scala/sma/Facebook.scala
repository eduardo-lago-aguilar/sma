package sma

import akka.actor.{Props, Actor, ActorLogging}
import sma.DiggingMessages.Follow

object Facebook {
  def props(): Props = {
    Props(classOf[Facebook])
  }
}

class Facebook extends Actor with ActorLogging {
  override def receive = {
    case f: Follow =>
      log.info(s"@facebook following ${f.mkString}")
      sender() ! f.reply
  }
}
