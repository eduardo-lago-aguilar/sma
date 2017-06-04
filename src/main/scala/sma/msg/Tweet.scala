package sma.msg

case class TweetTrackTerms(terms: Seq[String])

case class Tweet(body: String, trackTerms: Seq[String], timestamp: Long)

case class TweetReply()
