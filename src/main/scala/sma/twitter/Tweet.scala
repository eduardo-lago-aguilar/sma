package sma.twitter

import sma.eventsourcing.Receiving
import sma.json.Json

case class TrackingTerm(term: String)

case class Tweet(id: String, body: String, trackingTerms: Seq[String], hashTrackingTerms: String)

case class TweetReply()

object TweetJsonHelper extends Receiving {

  def decodeId(json: String): Option[Any] = Json.decode[Map[String, Any]](json).get("id_str")

  def decodeTweet(record: ConsumerRecordType): Tweet = Json.decode[Tweet](record.value())

}
