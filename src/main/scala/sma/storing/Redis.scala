package sma.storing

import akka.stream.scaladsl.Source
import redis.RedisClient
import sma.eventsourcing.EventSourcing

object Redis extends EventSourcing {
  val redis = RedisClient()

  def smembers(key: String) = redis.smembers[String](key)

  def smembersStream(key: String) = Source.fromFuture(smembers(key)).mapConcat(seq => seq.toStream)

  def sadd(key: String, interests: String*) = redis.sadd[String](key, interests: _*)

  def sremove(key: String, interests: String*) = redis.srem[String](key, interests: _*)
}
