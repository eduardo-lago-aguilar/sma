package sma.feeding

import akka.actor.Props
import sma.digging.{BulkDigging, DiggingReactive}

object Profiling {
  def props(): Props = {
    Props(classOf[Profiling])
  }
}

class Profiling(topic: String) extends DiggingReactive(topic) {
  override def receive = {
    case rtt: RetrieveTrackingTerms =>
      receiving(rtt.mkString)
      sender() ! TrackingTerms(trackingTerms.toVector)
    case bulk: BulkDigging =>
      super.proccess(bulk, true)
  }

}
