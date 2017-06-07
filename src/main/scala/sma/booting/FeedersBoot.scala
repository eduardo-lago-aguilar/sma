package sma.booting

import akka.actor.Props
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Source._
import sma.Settings
import sma.Settings.theUsers
import sma.eventsourcing.EventSourcing
import sma.reactive.ReactiveStreamWrapper
import sma.storing.Redis._
import sma.twitter.TwitterFeeder

trait FeedersBoot extends EventSourcing {
  def wakeupFeeders: Unit = {
    theUsers
      .runForeach(user => Source(networks.toVector)
        .runForeach(net => {
          val topic = replyTopic(digTopic(user, net))
          val name = s"${topic}_${TwitterFeeder.nick}"
          ReactiveStreamWrapper(system, name, Props(new TwitterFeeder(topic)))
        }))
  }

}
