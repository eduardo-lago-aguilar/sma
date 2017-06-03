package sma

import redis.RedisClient

object Redis extends Topics {
  private val redis = RedisClient()

  def smaUsers = redis.smembers[String]("sma-users")

  object Interests {
    def apply(user: String, network: String) = redis.smembers[String](key(user, network))
    def apply(topic: String) = redis.smembers[String](key(topic))

    def add(topic: String, interests: String*) = redis.sadd[String](key(topic), interests: _*)

    def remove(topic: String, interests: String*) = redis.srem[String](key(topic), interests: _*)

    def key(user: String, network: String): String = {
      key(digTopic(user, network))
    }

    def key(topic: String): String = {
      s"interests_${topic}"
    }
  }

}
