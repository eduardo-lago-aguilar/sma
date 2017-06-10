package sma

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._
import scala.util.Properties

object Settings {

  def theUsers = config.getStringList("users").asScala.toVector

  def networks = config.getStringList("networks").asScala.toVector

  def wakeupNetworkers = config.getBoolean("wakeup_networkers")

  def wakeupFeeders = config.getBoolean("wakeup_feeders")

  def wakeupProfilers = config.getBoolean("wakeup_profilers")

  def defaultUser = config.getString("default_user")

  def defaultNetwork = config.getString("default_network")

  object twitter {

    def consumerKey = Properties.envOrElse("TWITTER_CONSUMER_KEY", twitter.getString("consumer_key"))

    def consumerSecret = Properties.envOrElse("TWITTER_CONSUMER_SECRET", twitter.getString("consumer_secret"))

    def token = Properties.envOrElse("TWITTER_TOKEN", twitter.getString("token"))

    def tokenSecret = Properties.envOrElse("TWITTER_TOKEN_SECRET", twitter.getString("token_secret"))

    def messageQueueSize = twitter.getInt("message_queue_size")

    def heartbeatPeriod = twitter.getInt("heartbeat_period")

    def tweetsBatchSize = twitter.getInt("tweets_batch_size")

    private val twitter = config.getConfig("twitter")
  }

  object kafka {
    def bootstrapServers = kafka.getString("bootstrap_servers")

    def autoOffsetResetConfig = kafka.getString("auto_offset_rest_config")

    private val kafka = config.getConfig("kafka")
  }

  object http {
    def timeout = http.getInt("timeout")

    def host = http.getString("host")

    def port = http.getInt("port")

    private val http = config.getConfig("http")
  }

  object reactive {
    def timeout = reactive.getInt("timeout")

    def minBackoff = reactive.getInt("min_backoff")

    def maxBackoff = reactive.getInt("max_backoff")

    def randomFator = reactive.getInt("random_factor")

    private val reactive = config.getConfig("reactive")
  }

  object digging {
    def batchPeriod = digging.getInt("batch_period")

    def batchSize = digging.getInt("batch_size")

    private val digging = config.getConfig("digging")
  }

  object redis {
    def host = redis.getString("host")

    def port = redis.getInt("port")

    private val redis = config.getConfig("redis")
  }

  private val config: Config = ConfigFactory.parseFile(new File("application.conf"))
}
