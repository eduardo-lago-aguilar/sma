package sma.booting

import akka.actor.Props
import akka.stream.scaladsl.Sink.foreach
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Source.fromFuture
import sma.eventsourcing.EventSourcing
import sma.reactive.ReactiveStreamWrapper
import sma.storing.Redis._
import sma.twitter.TwitterNetworker

trait NetworkersBoot extends EventSourcing {
  def wakeupNetworkers: Unit = {
    fromFuture(smaUsers)
      .runWith(foreach(users => Source(users.toVector)
        .runWith(foreach(user => Source(networks.toVector)
          .runWith(foreach(net => ReactiveStreamWrapper(system, digTopic(user, net), Props(new TwitterNetworker(digTopic(user, net))))))))))
  }
}
