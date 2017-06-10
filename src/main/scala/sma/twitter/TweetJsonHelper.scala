package sma.twitter

import org.apache.kafka.clients.producer.ProducerRecord
import sma.eventsourcing.Receiving
import sma.json.Json

object TweetJsonHelper extends Receiving {

  def decodeId(json: String): Option[Any] = Json.decodeFromString[Map[String, Any]](json).get("id_str")

  def decodeTweet(record: ConsumerRecordType): Tweet = Json.decode[Tweet](record.value())

  def twitterProducerRecord(tweet: Tweet, targetTopic: String) = {
    val key = Json.encode(TrackingTerms(tweet.trackingTerms))
    val value = Json.encode(tweet)
    new ProducerRecord[Array[Byte], Array[Byte]](targetTopic, key, value)
  }

}
