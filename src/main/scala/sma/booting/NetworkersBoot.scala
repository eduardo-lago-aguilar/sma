package sma.booting

import akka.actor.Props
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Source.fromFuture
import sma.eventsourcing.EventSourcing
import sma.reactive.ReactiveStreamWrapper
import sma.storing.Redis._
import sma.twitter.TwitterNetworker

trait NetworkersBoot extends EventSourcing {
  def wakeupNetworkers: Unit = {
    fromFuture(smaUsers)
      .runForeach(users => Source(users.toVector)
        .runForeach(user => Source(networks.toVector)
          .runForeach(net => {
            val topic = digTopic(user, net)
            val name = s"${topic}_${TwitterNetworker.nick}"
            ReactiveStreamWrapper(system, name, Props(new TwitterNetworker(topic)))
          })))
  }
}
