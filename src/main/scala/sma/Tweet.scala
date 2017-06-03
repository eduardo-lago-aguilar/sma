package sma

case class Tweet(body: String) extends StringSerializableMessage {
  override def key = null

  override def serialize = body
}

case class TweetReply()
