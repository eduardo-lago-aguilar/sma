package sma.msg

case class Digging(follower: String, interest: String, action: String) {

  def followee = interest.split("@")(0)

  def network = interest.split("@")(1)

  def key: String = s"${follower}!${interest}"

  def mkString = key

  // TODO: DRY!
  def digTopic: String = {
    s"${follower}_at_${network}"
  }

}

case class DiggingReply()

case class DiggingBulk(messages: Seq[Digging]) {
  def apply() = messages

  def serialize = messages.mkString(", ")
}

case class DiggingBulkReply()
