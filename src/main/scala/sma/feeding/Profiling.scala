package sma.feeding

import sma.digging.{BulkDigging, DiggingReactive}

class Profiling(topic: String) extends DiggingReactive(topic) {
  override def receive = {
    case bulk: BulkDigging =>
      super.proccess(bulk, true)
  }

  override def consumerGroup = s"${self.path.name}__profiling"

}
