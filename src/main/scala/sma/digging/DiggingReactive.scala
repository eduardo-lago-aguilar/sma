package sma.digging

import akka.Done
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.{Sink, Source}
import sma.eventsourcing.Receiving
import sma.json.Json
import sma.reactive.ReactiveWrappedActor
import akka.pattern.ask
import sma.storing.Redis
import sma.storing.Redis.InterestsStore

import scala.collection.immutable.SortedSet
import scala.concurrent.Future

abstract class DiggingReactive(topic: String) extends ReactiveWrappedActor with Receiving {

  var trackingTerms = SortedSet[String]()

  override def consume: Future[Done] = {
    Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))
      .map(record => digging(record))
      .groupedWithin(batchSize, batchPeriod)
      .mapAsync(1)(bulk => self ? BulkDigging(bulk))
      .runWith(Sink.ignore)
  }

  def proccess(bulk: BulkDigging, storing: Boolean = false) = {
    receiving(bulk.serialize)
    Source(bulk().toVector)
      .runWith(Sink.foreach[Digging](dig => digProccess(dig, storing)))
    sender() ! BulkDiggingReply()
  }

  private def digging(record: ConsumerRecordType): Digging = Json.decode[Digging](record.value())

  private def digProccess(dig: Digging, storing: Boolean = false): Unit = {
    dig.action match {
      case "follow" => {
        trackingTerms += dig.followee
        if (storing) {
          InterestsStore.add(topic, dig.followee)
        }
      }
      case "forget" => {
        trackingTerms -= dig.followee
        if(storing) {
          InterestsStore.remove(topic, dig.followee)
        }
      }
    }
  }
}
