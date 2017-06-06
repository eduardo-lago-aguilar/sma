package sma.digging

case class Digging(user: String, network: String, term: String, action: String) {

  val key: String = s"${term}!${user}@${network}"

  def mkString = key

}

case class DiggingReply()

case class BulkDigging(messages: Seq[Digging]) {
  def apply() = messages

  def mkString = messages.mkString(", ")
}

case class BulkDiggingReply()
