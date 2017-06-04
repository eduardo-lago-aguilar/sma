package sma

import akka.actor.Props
import akka.stream.scaladsl.Sink.foreach
import akka.stream.scaladsl.Source.fromFuture
import akka.stream.scaladsl.{Source, Sink}
import sma.Redis._
import sma.reactive.ReactiveStreamWrapper
import sma.twitter.TwitterNetworker

trait Networkers extends EventSourcing {
  def wakeupNetworkers: Unit = {
    fromFuture(smaUsers)
      .runWith(foreach(users => Source(users.toVector)
        .runWith(foreach(user => Source(networks.toVector)
          .runWith(foreach(net => ReactiveStreamWrapper(system, digTopic(user, net), Props(new TwitterNetworker(digTopic(user, net))))))))))
  }
}
