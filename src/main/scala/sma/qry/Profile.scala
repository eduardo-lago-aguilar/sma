package sma.qry

import akka.actor.{Actor, ActorLogging, Props}
import sma.Redis
import sma.qry.QueryMessages.Interests

object Profile {
  def props(): Props = {
    Props(classOf[Profile])
  }
}

class Profile extends Actor with ActorLogging {
  override def receive = {
    case t: Interests =>
      log.info(s"received ${t.mkString}")
      sender() ! t.reply(Redis.Interests(t.user, t.network))
  }

}
