package sma

import akka.actor.Props
import sma.Redis._
import sma.cmd.TwitterNetworker

trait Networkers extends Receiving {
  val networks = Seq("twitter")

  def wakeupNetworkers: Unit = {
    smaUsers.foreach(users => {
      for (user <- users) {
        for (net <- networks) {
          val topic: String = digTopic(user, net)
          ReactiveStreamWrapper(system, topic, Props(new TwitterNetworker(topic)))
          //          wake(replyTopic(topic))
        }
      }
    })
  }


}
