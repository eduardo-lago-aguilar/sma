package sma.booting

import akka.actor.Props
import akka.stream.scaladsl.Sink._
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Source._
import sma.eventsourcing.EventSourcing
import sma.reactive.ReactiveStreamWrapper
import sma.storing.Redis._
import sma.twitter.TwitterFeeder

trait FeedersBoot extends EventSourcing {
  def wakeupFeeders: Unit = {
    fromFuture(smaUsers)
      .runWith(foreach(users => Source(users.toVector)
        .runWith(foreach(user => Source(networks.toVector)
          .runWith(foreach(net => ReactiveStreamWrapper(system, replyTopic(digTopic(user, net)), Props(new TwitterFeeder(replyTopic(digTopic(user, net)))))))))))
  }

}
