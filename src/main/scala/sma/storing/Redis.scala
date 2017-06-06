package sma.storing

import redis.RedisClient
import sma.eventsourcing.Topics

object Redis extends Topics {
  private val redis = RedisClient()

  def smaUsers = redis.smembers[String]("sma-users")

  object TrackingTermsStore {
    // retrieve
    def apply(digTopic: String) = redis.smembers[String](key(digTopic))

    // add/remove
    def add(topic: String, interests: String*) = redis.sadd[String](key(topic), interests: _*)

    def remove(topic: String, interests: String*) = redis.srem[String](key(topic), interests: _*)

    private def key(user: String, network: String): String = {
      key(digTopic(user, network))
    }

    private def key(topic: String): String = {
      s"interests_${topic}"
    }
  }

  object MessagesStore {
    // retrieve
    def apply(trackingTermsTopic: String) = redis.smembers[String](trackingTermsTopic)

    // add
    def add(trackingTermsTopic: String, messages: String*) = redis.sadd[String](trackingTermsTopic, messages: _*)
  }

}
