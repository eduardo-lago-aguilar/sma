package sma.twitter

case class TrackingTerm(term: String)

case class TrackingTerms(terms: Seq[String])

case class Tweet(id: String, body: String, trackingTerms: Seq[String], hashTrackingTerms: String)

case class TweetReply()
