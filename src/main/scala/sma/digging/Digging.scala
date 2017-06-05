package sma.digging

case class Digging(follower: String, interest: String, action: String) {

  def followee = interest.split("@")(0)

  def network = interest.split("@")(1)

  def key: String = s"${follower}!${interest}"

  def mkString = key

}

case class DiggingReply()

case class BulkDigging(messages: Seq[Digging]) {
  def apply() = messages

  def serialize = messages.mkString(", ")
}

case class BulkDiggingReply()
