package sma.twitter

import org.apache.kafka.clients.producer.ProducerRecord
import sma.eventsourcing.{Receiving, TrackingTerms}
import sma.json.Json

case class TrackingTerm(term: String)

case class Tweet(id: String, body: String, trackingTerms: Seq[String], hashTrackingTerms: String)

case class TweetReply()

object TweetJsonHelper extends Receiving {

  def decodeId(json: String): Option[Any] = Json.decode[Map[String, Any]](json).get("id_str")

  def decodeTweet(record: ConsumerRecordType): Tweet = Json.decode[Tweet](record.value())

  def twitterProducerRecord(tweet: Tweet, targetTopic: String) = {
    val key = Json.encode(TrackingTerms(tweet.trackingTerms))
    val value = Json.encode(tweet)
    new ProducerRecord[Array[Byte], Array[Byte]](targetTopic, key, value)
  }

}
