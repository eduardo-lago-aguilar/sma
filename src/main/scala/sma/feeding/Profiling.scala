package sma.feeding

import sma.digging.{BulkDigging, BulkDiggingReply, DiggingReactive}
import sma.storing.Redis.{sadd, sremove}

object Profiling {
  val nick = "profiler"
}

class Profiling(topic: String) extends DiggingReactive(topic) {
  override def receive = {
    case bulk: BulkDigging =>
      sender() ! BulkDiggingReply()
      super.proccess(bulk)
  }

  override def consumerGroup = s"${self.path.name}_${Profiling.nick}"

  override def doFollow(term: String): Unit = {
    super.doFollow(term)
    sadd(topic, term)
  }

  override def doForget(term: String): Unit = {
    super.doForget(term)
    sremove(topic, term)
  }
}
