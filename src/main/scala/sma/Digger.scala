package sma

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import sma.DiggingMessages._

object Digger {
  def props(): Props = {
    Props(classOf[Digger])
  }
}

class Digger(twitter: ActorRef, facebook: ActorRef) extends Actor with ActorLogging {

  val medias = Map("twitter" -> twitter, "facebook" -> facebook)

  override def receive = {
    case f: Digging =>
      forward(f, f.media)

      log.info(s"received ${f.mkString}")
      sender() ! f.reply
  }

  def forward(message: Digging, media: String) = {
    medias(media) ! message
  }
}
