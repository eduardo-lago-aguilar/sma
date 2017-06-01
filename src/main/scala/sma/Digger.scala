package sma

import akka.actor.{Actor, ActorLogging, Props}
import sma.DiggingMessages._

object Digger {
  def props(): Props = {
    Props(classOf[Digger])
  }
}

class Digger extends Actor with ActorLogging {
  override def receive = {
    case Follow(follewer, interest) =>

      val Array(followee, media) = interest.split("@")
      val follow = Follow(follewer, followee)
      forward(follow, media)

      log.info(s"received follow request, follower: ${follewer}, interest: ${interest}")
      sender() ! FollowReply()
    case Forget(follewer, interest) =>
      log.info(s"received forget request, follower: ${follewer}, interest: ${interest}")
      sender() ! ForgetReply()
  }

  def forward(message: Digging, topic: String) = ???
}
