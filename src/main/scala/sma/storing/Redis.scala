package sma.storing

import redis.RedisClient
import sma.eventsourcing.Topics

object Redis extends Topics {
  private val redis = RedisClient()

  def smaUsers = redis.smembers[String]("sma-users")

  object InterestsStore {
    // retrieve
    def apply(user: String, network: String) = redis.smembers[String](key(user, network))
    def apply(topic: String) = redis.smembers[String](key(topic))

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

}
