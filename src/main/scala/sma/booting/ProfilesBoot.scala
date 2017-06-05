package sma.booting

import akka.actor.Props
import akka.stream.scaladsl.Sink._
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Source._
import sma.eventsourcing.{ProfileActors, EventSourcing}
import sma.feeding.Profiling
import sma.reactive.ReactiveStreamWrapper
import sma.storing.Redis._
import sma.twitter.TwitterFeeder
import sun.java2d.cmm.Profile

trait ProfilesBoot extends EventSourcing with ProfileActors{
  def wakeupProfiles: Unit = {
    fromFuture(smaUsers)
      .runWith(foreach(users => Source(users.toVector)
        .runWith(foreach(user => Source(networks.toVector)
          .runWith(foreach(net => {
            profiles = profiles + (digTopic(user, net) -> ReactiveStreamWrapper(system, digTopic(user, net), Props(new Profiling(digTopic(user, net)))))
          }))))))
  }

}
