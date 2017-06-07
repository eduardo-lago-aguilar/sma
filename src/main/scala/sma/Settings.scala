package sma

import java.io.File

import akka.stream.scaladsl.Source
import com.twitter.hbc.httpclient.auth.OAuth1
import com.typesafe.config.ConfigFactory

import scala.util.Properties

object Settings {

  val theUsers = Vector("ed", "herve", "olivia", "nicolas")

  val networks = Vector("twitter")

  val twitter = ConfigFactory.parseFile(new File("application.conf")).getConfig("twitter")

  def consumerKey = Properties.envOrElse("TWITTER_CONSUMER_KEY", twitter.getString("consumer_key"))

  def consumerSecret = Properties.envOrElse("TWITTER_CONSUMER_SECRET", twitter.getString("consumer_secret"))

  def token = Properties.envOrElse("TWITTER_TOKEN", twitter.getString("token"))

  def tokenSecret = Properties.envOrElse("TWITTER_TOKEN_SECRET", twitter.getString("token_secret"))

  def oAuth1 = new OAuth1(consumerKey, consumerSecret, token, tokenSecret)
}
