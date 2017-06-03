package sma

import akka.actor.Props
import akka.stream.scaladsl.Sink.foreach
import akka.stream.scaladsl.Source.fromFuture
import akka.stream.scaladsl.{Source, Sink}
import sma.Redis._
import sma.cmd.TwitterNetworker
import sma.reactive.ReactiveStreamWrapper

trait Networkers extends Receiving {
  def wakeupNetworkers: Unit = {
    fromFuture(smaUsers)
      .runWith(foreach(users => Source(users.toVector)
        .runWith(foreach(user => Source(networks.toVector)
          .runWith(foreach(net => ReactiveStreamWrapper(system, digTopic(user, net), Props(new TwitterNetworker(digTopic(user, net))))))))))
  }
}
