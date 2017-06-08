package sma.feeding

import sma.digging.{BulkDiggingReply, BulkDigging, DiggingReactive}
import sma.storing.Redis

import scala.concurrent.Future

object Profiling {
  val nick = "profiler"
}

class Profiling(topic: String) extends DiggingReactive(topic) {
  override def receive = {
    case bulk: BulkDigging =>
      super.proccess(bulk)
      sender() ! BulkDiggingReply()
  }

  override def consumerGroup = s"${self.path.name}_${Profiling.nick}"

  override def doFollow(term: String): Unit = {
    super.doFollow(term)
    Redis.sadd(topic, term)
  }

  override def doForget(term: String): Unit = {
    super.doForget(term)
    Redis.sremove(topic, term)
  }
}
