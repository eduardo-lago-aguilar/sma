package sma

import akka.actor.{ActorRef, ActorSystem}
import sma.Redis._
import sma.WebServerHttpApp._
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

  def wakeupNetworkers: Unit = {
    smaUsers.foreach(users => {
      for (user <- users) {
        for ((net, streamWrapper) <- supervisors) {
          wakeup(user, net)
        }
      }
    })
  }


}
