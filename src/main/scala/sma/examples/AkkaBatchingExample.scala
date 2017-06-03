package sma.examples

import akka.stream.scaladsl.{Sink, Source}
import sma.EventSourcing

import scala.concurrent.duration._
import scala.util.Random


case class Sample(ms: Long, data: Float)

object AkkaBatchingExample extends App with EventSourcing {
  Source.tick(0 milliseconds, 10 milliseconds, ())
    .map(_ => Sample(System.currentTimeMillis(), Random.nextFloat()))
    .groupedWithin(1000, 100 milliseconds)
    .map(bulk => bulk.getClass.getName)
    .runWith(Sink.foreach(println))
}
