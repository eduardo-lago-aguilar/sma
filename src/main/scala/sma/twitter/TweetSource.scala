package sma.twitter

import java.util.concurrent.LinkedBlockingQueue

import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.core.{Constants, HttpHosts}
import com.twitter.hbc.httpclient.auth.OAuth1
import sma.Settings
import sma.eventsourcing.EventSourcing

import scala.collection.JavaConverters

object TweetSource {
  def oAuth1 = new OAuth1(Settings.twitter.consumerKey, Settings.twitter.consumerSecret, Settings.twitter.token, Settings.twitter.tokenSecret)
}

class TweetSource(trackingTerms: Seq[String]) extends EventSourcing {
  val msgQueue = new LinkedBlockingQueue[String](Settings.twitter.messageQueueSize)

  val hosebirdEndpoint = new StatusesFilterEndpoint()
  hosebirdEndpoint.trackTerms(JavaConverters.seqAsJavaList(trackingTerms))

  val builder = new ClientBuilder()
    .hosts(new HttpHosts(Constants.STREAM_HOST))
    .authentication(TweetSource.oAuth1)
    .endpoint(hosebirdEndpoint)
    .processor(new StringDelimitedProcessor(msgQueue))

  val hosebirdClient = builder.build()
  hosebirdClient.connect()

  def take(): Option[String] = {
    if (hosebirdClient.isDone)
      None
    else
      Some(msgQueue.take())
  }

  def iterate(size: Int, f: (String) => Any, cancelling: () => Boolean): Int = {
    var count = 0
    var cancel = cancelling()
    while ((!hosebirdClient.isDone) && !closingApp && count < size && !cancel) {
      take() match {
        case Some(json) =>
          f(json)
          count = count + 1
        case None =>
      }
      cancel = cancelling()
    }
    if (cancel) {
      println("cancelling twitter streaming!")
    }
    count
  }

  def close = hosebirdClient.stop
}
