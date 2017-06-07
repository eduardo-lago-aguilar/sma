package sma.storing

import akka.NotUsed
import akka.stream.scaladsl.Source
import redis.RedisClient
import sma.eventsourcing.Topics

import scala.concurrent.Future

object Redis extends Topics {
  val redis = RedisClient()

  def smaUsers = redis.smembers[String]("sma-users")

  def smembers(key: String) = redis.smembers[String](key)

  def smembersStream(key: String) = Source.fromFuture(smembers(key)).mapConcat(seq => seq.toStream)

  def sadd(key: String, interests: String*) = redis.sadd[String](key, interests: _*)

  def sremove(key: String, interests: String*) = redis.srem[String](key, interests: _*)

  def lrange(key: String): Future[Seq[String]] = redis.lrange[String](key, 0, -1)

  def lrangeStream(key: String): Source[String, NotUsed] = Source.fromFuture(lrange(key)).mapConcat(seq => seq.toStream)
}
