package sma

import akka.actor.Props
import akka.stream.scaladsl.Sink._
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Source._
import sma.Redis._
import sma.cmd.TwitterFeeder
import sma.reactive.ReactiveStreamWrapper

trait Feeders extends EventSourcing {
  def wakeupFeeders: Unit = {
    fromFuture(smaUsers)
      .runWith(foreach(users => Source(users.toVector)
        .runWith(foreach(user => Source(networks.toVector)
          .runWith(foreach(net => ReactiveStreamWrapper(system, replyTopic(digTopic(user, net)), Props(new TwitterFeeder(replyTopic(digTopic(user, net)))))))))))
  }

}
