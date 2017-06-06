package sma.feeding

import sma.digging.{BulkDiggingReply, BulkDigging, DiggingReactive}

object Profiling {
  val nick = "profiler"
}

class Profiling(topic: String) extends DiggingReactive(topic) {
  override def receive = {
    case bulk: BulkDigging =>
      super.proccess(bulk, true)
      sender() ! BulkDiggingReply()
  }

  override def consumerGroup = s"${self.path.name}_${Profiling.nick}"

}
