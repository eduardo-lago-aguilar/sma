package sma

import akka.actor.{ActorRef, ActorSystem}
import sma.cmd.StreamWrapperTwitter

trait StreamWrapper {
  def create(implicit system: ActorSystem, childName: String): ActorRef
}

trait Networkers extends Receiving {
  val networks = Seq("twitter")
  val supervisors: Map[String, StreamWrapper] = Map(
    ("twitter" -> StreamWrapperTwitter)
  )

  def wakeup(follower: String, network: String) = {
    StreamWrapperTwitter.create(system, digTopic(follower, network))
  }

}
