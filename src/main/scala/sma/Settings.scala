package sma

import java.io.File

import com.twitter.hbc.httpclient.auth.OAuth1
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

    def oAuth1 = new OAuth1(consumerKey, consumerSecret, token, tokenSecret)

    private val twitter = config.getConfig("twitter")
  }


  private val config: Config = ConfigFactory.parseFile(new File("application.conf"))
}
