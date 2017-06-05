package sma.digging

import sma.eventsourcing.Topics

case class Digging(follower: String, interest: String, action: String) extends Topics {

  def term = splittingTermAt(interest)(0)

  def network = splittingTermAt(interest)(1)

  def key: String = s"${follower}!${interest}"

  def mkString = key

}

case class DiggingReply()

case class BulkDigging(messages: Seq[Digging]) {
  def apply() = messages

  def serialize = messages.mkString(", ")
}

case class BulkDiggingReply()
