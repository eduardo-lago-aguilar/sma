package sma.twitter

case class TrackingTerm(text: String)

case class Tweet(id: String, body: String, trackingTerms: Seq[String], timestamp: Long)

case class TweetReply()
