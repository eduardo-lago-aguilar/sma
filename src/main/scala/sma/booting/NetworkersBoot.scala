package sma.booting

import akka.actor.Props
import akka.stream.scaladsl.Source
import sma.Settings._
import sma.eventsourcing.EventSourcing
import sma.reactive.ReactiveStreamWrapper
import sma.twitter.TwitterNetworker

trait NetworkersBoot extends EventSourcing {
  def wakeupNetworkers: Unit = {
    Source(theUsers)
      .runForeach(user => Source(networks)
        .runForeach(net => {
          val topic = digTopic(user, net)
          val name = s"${topic}_${TwitterNetworker.nick}"
          ReactiveStreamWrapper(name, Props(new TwitterNetworker(topic)))
        }))
  }
}
