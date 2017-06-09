package sma.digging

import akka.Done
import akka.pattern.ask
import akka.stream.scaladsl.{Sink, Source}
import sma.eventsourcing.Receiving
import sma.json.Json
import sma.reactive.ReactiveWrappedActor

import scala.collection.immutable.SortedSet
import scala.concurrent.Future

abstract class DiggingReactive(topic: String) extends ReactiveWrappedActor with Receiving {

  var trackingTerms = SortedSet[String]()
  var lastVersion = 0

  override def consume: Future[Done] = {
    plainSource(topic, consumerGroup)
      .map(record => digging(record))
      .groupedWithin(batchSize, batchPeriod)
      .mapAsync(1)(bulk => {
        lastVersion += 1
        self ? BulkDigging(bulk, lastVersion)
      })
      .runWith(Sink.ignore)
  }

  def proccess(bulk: BulkDigging) = {
    logReceiving(bulk.mkString)
    Source(bulk().toVector)
      .runForeach(dig => digProccess(dig))
  }

  def consumerGroup: String

  def doFollow(term: String): Unit = {
    trackingTerms += term
  }

  def doForget(term: String): Unit = {
    trackingTerms -= term
  }

  private def digging(record: ConsumerRecordType): Digging = Json.decode[Digging](record.value())

  private def digProccess(dig: Digging): Unit = {
    dig.action match {
      case "follow" => doFollow(dig.term)
      case "forget" => doForget(dig.term)
    }
  }

}
