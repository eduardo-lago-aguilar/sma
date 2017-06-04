package sma.feeding

import akka.actor.{Actor, ActorLogging, Props}
import sma.storing.Redis._

object Feed {
  def props(): Props = {
    Props(classOf[Feed])
  }
}

class Feed extends Actor with ActorLogging {
  override def receive = {
    case t: Interests =>
      log.info(s"received ${t.mkString}")
      sender() ! t.reply(InterestsStore(t.user, t.network))
  }

}
