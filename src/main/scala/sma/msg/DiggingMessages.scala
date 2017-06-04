package sma.msg

import sma.StringSerializableMessage

object Digging {
  def deserialize(msg: String): Digging = {
    val Array(action, remaining) = msg.split("#")
    val Array(follower, interest) = remaining.split("!")
    action match {
      case "follow" => Follow(follower, interest)
      case "forget" => Forget(follower, interest)
    }
  }
}

abstract class Digging(follower: String, interest: String) extends StringSerializableMessage {

  def followee = interest.split("@")(0)

  def network = interest.split("@")(1)

  def reply: DiggingReply

  override def key: String = s"${follower}!${interest}"

  def mkString = serialize

  def serialize = s"${action}#${key}"

  def digTopic: String = {
    s"${follower}_at_${network}"
  }


  def action: String
}

abstract class DiggingReply

case class Follow(follower: String, interest: String) extends Digging(follower: String, interest: String) {

  override def reply = FollowReply()

  override def action = "follow"
}

case class FollowReply() extends DiggingReply

case class Forget(follower: String, interest: String) extends Digging(follower: String, interest: String) {
  override def reply = ForgetReply()

  override def action = "forget"
}

case class ForgetReply() extends DiggingReply

case class DiggingBulk(messages: Seq[Digging]) {
  def apply() = messages

  def serialize = messages.mkString(", ")
}

case class DiggingBulkReply()