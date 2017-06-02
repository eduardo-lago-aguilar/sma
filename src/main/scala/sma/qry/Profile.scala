package sma.qry

import akka.actor.{Props, ActorLogging, Actor}
import sma.qry.QueryMessages.Topics

object Profile {
  def props(): Props = {
    Props(classOf[Profile])
  }
}

class Profile extends Actor with ActorLogging {
  override def receive = {
    case t: Topics =>

      log.info(s"received ${t.mkString}")
      sender() ! t.reply(Seq("#redbee", "#akka", "#tdd"))
  }

}
