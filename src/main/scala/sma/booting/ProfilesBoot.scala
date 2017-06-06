package sma.booting

import akka.actor.Props
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Source._
import sma.eventsourcing.EventSourcing
import sma.feeding.Profiling
import sma.reactive.ReactiveStreamWrapper
import sma.storing.Redis._

trait ProfilesBoot extends EventSourcing {
  def wakeupProfiles: Unit = {
    fromFuture(smaUsers)
      .runForeach(users => Source(users.toVector)
        .runForeach(user => Source(networks.toVector)
          .runForeach(net => {
            val topic: String = digTopic(user, net)
            val name = s"${topic}_${Profiling.nick}"
            ReactiveStreamWrapper(system, name, Props(new Profiling(topic)))
          })))
  }

}
