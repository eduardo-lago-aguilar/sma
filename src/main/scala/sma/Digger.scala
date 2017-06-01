package sma

import akka.actor.{Actor, ActorLogging, Props}
import sma.DiggingMessages._

object Digger {
  def props(): Props = {
    Props(classOf[Digger])
  }
}

class Digger extends Actor with ActorLogging {
  override def receive = {
    case f: Digging =>
      forward(f, f.media)

      log.info(s"received ${f.mkString}")
      sender() ! f.reply
  }

  def forward(message: Digging, topic: String) = {
    println("fowarding message!")
  }
}
