package sma.cmd

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.apache.kafka.clients.producer.ProducerRecord
import sma.Committing
import sma.cmd.DiggingMessages._

object Digger {
  def props(): Props = {
    Props(classOf[Digger])
  }
}

class Digger(twitter: ActorRef, facebook: ActorRef) extends Actor with ActorLogging with Committing {

  val medias = Map("twitter" -> twitter, "facebook" -> facebook)

  override def receive = {
    case f: Digging =>
      forward(f, f.media)

      log.info(s"received ${f.mkString}")
      sender() ! f.reply
  }

  def forward(message: Digging, media: String) = {
//    kafkaProducer.send(new ProducerRecord[String, Array[Byte]]("topic2", new Array[Byte]()))
    medias(media) ! message
  }
}
