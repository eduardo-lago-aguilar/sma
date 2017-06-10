package sma.profiling

import sma.digging.{BulkDigging, BulkDiggingReply, DiggingReactiveActor}
import sma.storing.Redis.{sadd, sremove}

object ProfilingActor {
  val nick = "profiler"
}

class ProfilingActor(topic: String) extends DiggingReactiveActor(topic) {
  override def receive = {
    case bulk: BulkDigging =>
      sender() ! BulkDiggingReply()
      super.proccess(bulk)
  }

  override def consumerGroup = s"${self.path.name}_${ProfilingActor.nick}"

  override def doFollow(term: String): Unit = {
    super.doFollow(term)
    sadd(topic, term)
  }

  override def doForget(term: String): Unit = {
    super.doForget(term)
    sremove(topic, term)
  }
}
