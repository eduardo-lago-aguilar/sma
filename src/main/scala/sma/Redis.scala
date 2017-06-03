package sma

import redis.RedisClient

object Redis extends Topics {
  private val redis = RedisClient()

  def smaUsers = redis.smembers[String]("sma-users")

  object Interests {
    def apply(user: String, network: String) = redis.smembers[String](key(user, network))
    def add(user: String, network: String, interest: String) = redis.sadd[String](key(user, network), interest)
    def remove(user: String, network: String, interest: String) = redis.srem[String](key(user, network), interest)

    private def key(user: String, network: String): String = {
      s"interests_${digTopic(user, network)}"
    }
  }

}
