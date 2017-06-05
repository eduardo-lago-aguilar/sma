package sma.feeding

import akka.actor.{Actor, ActorLogging, Props}
import sma.eventsourcing.Particle
import sma.storing.Redis._

object Feed {
  def props(): Props = {
    Props(classOf[Feed])
  }
}

class Feed extends Particle {
  override def receive = {
    case t: Interests =>
      receiving(t.mkString)
      sender() ! t.reply(InterestsStore(t.user, t.network))
  }

}
