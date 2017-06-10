package sma.twitter

import akka.actor.SupervisorStrategy.Restart
import akka.actor._
import akka.pattern.ask
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import sma.Settings
import sma.digging.{BulkDigging, BulkDiggingReply, DiggingReactive}
import sma.eventsourcing.Hash._
import sma.eventsourcing.{Committing, TrackingTerms}
import sma.json.Json

import scala.concurrent.duration._

object TwitterNetworker {
  val nick = "twitter_networker"
}

class TwitterNetworker(val topic: String) extends DiggingReactive(topic) with Committing {

  val heartbeatPeriod = 15 seconds
  val maxNumberOfTweets = 200
  private var streaming = false

  override def receive = {
    case heartbeat: Heartbeat =>
      sender() ! HeartbeatReply()
      logReceiving(s"heartbeat, current tracking terms: [${trackingTerms.mkString(", ")}]")
      streamFromTwitter(heartbeat.version)
    case bulk: BulkDigging =>
      sender() ! BulkDiggingReply()
      super.proccess(bulk)
      streamFromTwitter(bulk.version)
  }

  override def preStart: Unit = {
    heartbeat
    super.preStart
  }

  override def consumerGroup = s"${self.path.name}_${TwitterNetworker.nick}"

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 30 seconds) {
      case _: Exception                => Restart
    }


  private def heartbeat: Unit = {
    Source.tick(0 milliseconds, heartbeatPeriod, ())
      .async
      .runForeach(_ => {
        if(!streaming) {
          // only heartbeats when not streaming
          self ? Heartbeat(lastVersion)
        }
      })
  }

  private def streamFromTwitter(version: Int): Unit = {
    val cancelling = () => version < lastVersion
    if (trackingTerms.size > 0 && !cancelling()) {
      streaming = true
      log.info(s"--> [${self.path.name}] streaming from twitter to |${topic}| w/ terms: (${trackingTerms.mkString(", ")})")

      val tweetSource = new TweetSource(Settings.oAuth1, trackingTerms.toVector)
      val count = tweetSource.iterate(maxNumberOfTweets, storeTweet(sha256(trackingTerms.toSeq)), cancelling)

      log.info(s"--> [${self.path.name}] finishing streaming from twitter to |${topic}| w/ terms: (${trackingTerms.mkString(", ")}), brought ${count} messages!")

      tweetSource.close
    } else {
      if(trackingTerms.size == 0) {
        log.info(s"--> [${self.path.name}] giving up since no tracking terms !")
      }
    }
    streaming = false
  }

  private def storeTweet(hashTrackingTerms: String)(json: String) = {
    TweetJsonHelper.decodeId(json) match {
      case Some(id) =>
        val tweet = Tweet(id.toString, json, trackingTerms.toSeq, hashTrackingTerms)
        log.info(s"R E C E I V I N G  T W E E T  F R O M  T W I T T E R  A P I  W I T H  I D  =  ${id.toString}")
        producer.send(twitterProducerRecord(tweet, replyTopic(topic)))
      case None =>
        log.error("unable to decode tweet JSON")
    }
  }

  private def twitterProducerRecord(tweet: Tweet, targetTopic: String) = {
    val key = Json.encode(TrackingTerms(tweet.trackingTerms))
    val value = Json.encode(tweet)
    new ProducerRecord[Array[Byte], Array[Byte]](targetTopic, key, value)
  }

}

