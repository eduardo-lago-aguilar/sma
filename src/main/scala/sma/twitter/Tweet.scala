package sma.twitter

case class TrackTerms(terms: Seq[String])

case class Tweet(body: String, trackTerms: Seq[String], timestamp: Long)

case class TweetReply()
