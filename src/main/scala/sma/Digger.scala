package sma

import akka.actor.{Actor, ActorLogging, Props}
import sma.DiggingMessages.{ForgetReply, Forget, FollowReply, Follow}

object Digger {
  def props(): Props = {
    Props(classOf[Digger])
  }
}

class Digger extends Actor with ActorLogging {
  override def receive = {
    case Follow(follewer, interest) =>
      log.info(s"received follow request, follower: ${follewer}, interest: ${interest}")
      sender() ! FollowReply()
    case Forget(follewer, interest) =>
      log.info(s"received forget request, follower: ${follewer}, interest: ${interest}")
      sender() ! ForgetReply()
  }
}
