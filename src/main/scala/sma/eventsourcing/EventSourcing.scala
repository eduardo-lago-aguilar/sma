package sma.eventsourcing

import akka.stream.ActorMaterializer

trait EventSourcing extends Topics {
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val bootstrapServers: String = "localhost:9092"

  def timestamp: Long = System.currentTimeMillis()
}
