package sma

import akka.actor.{Actor, ActorLogging, Props}

object Digger {
  def props(): Props = {
    Props(classOf[Digger])
  }
}

abstract class Digging(follower: String, interest: String)

case class Follow(follower: String, interest: String) extends Digging(follower: String, interest: String)

case class FollowReply()

case class Forget(follower: String, interest: String) extends Digging(follower: String, interest: String)

case class ForgetReply()

class Digger extends Actor with ActorLogging {
  override def receive = {
    case Follow(follewer, interest) =>
      log.debug(s"received follow request, follower: ${follewer}, interest: ${interest}")
      sender() ! FollowReply()
    case Forget(follewer, interest) =>
      log.debug(s"received forget request, follower: ${follewer}, interest: ${interest}")
      sender() ! ForgetReply()
  }
}
