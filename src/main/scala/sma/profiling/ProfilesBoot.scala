package sma.profiling

import akka.actor.Props
import akka.stream.scaladsl.Source
import sma.Settings._
import sma.eventsourcing.EventSourcing
import sma.reactive.ReactiveStreamWrapper

trait ProfilesBoot extends EventSourcing {
  def wakeupProfiles: Unit = {
    Source(theUsers)
      .runForeach(user => Source(networks)
        .runForeach(net => {
          val topic: String = digTopic(user, net)
          val name = s"${topic}_${ProfilingActor.nick}"
          ReactiveStreamWrapper(system, name, Props(new ProfilingActor(topic)))
        }))
  }

}
