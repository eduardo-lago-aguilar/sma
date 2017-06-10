package sma.booting

import akka.actor.Props
import akka.stream.scaladsl.Source
import sma.Settings.{networks, theUsers}
import sma.eventsourcing.EventSourcing
import sma.reactive.ReactiveStreamWrapper
import sma.twitter.TwitterFeeder

trait FeedersBoot extends EventSourcing {
  def wakeupFeeders: Unit = {
    Source(theUsers)
      .runForeach(user => Source(networks)
        .runForeach(net => {
          val topic = replyTopic(digTopic(user, net))
          val name = s"${topic}_${TwitterFeeder.nick}"
          ReactiveStreamWrapper(name, Props(new TwitterFeeder(topic)))
        }))
  }

}
