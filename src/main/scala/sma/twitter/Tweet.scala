package sma.twitter

import sma.json.Json

case class TrackingTerm(term: String)

case class Tweet(id: String, body: String, trackingTerms: Seq[String], timestamp: Long, hashTrackingTerms: String)

case class TweetReply()

object TweetJsonHelper {
  def decodeId(json: String): Option[Any] = Json.decode[Map[String, Any]](json).get("id_str")
}
