package sma.twitter

import java.util.concurrent.LinkedBlockingQueue

import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.core.{Constants, HttpHosts}
import com.twitter.hbc.httpclient.auth.OAuth1
import sma.eventsourcing.EventSourcing

import scala.collection.JavaConverters

class TweetSource(oAuth1: OAuth1, trackingTerms: Seq[String]) extends EventSourcing {
  val msgQueue = new LinkedBlockingQueue[String](2000)

  val hosebirdEndpoint = new StatusesFilterEndpoint()
  hosebirdEndpoint.trackTerms(JavaConverters.seqAsJavaList(trackingTerms))

  val builder = new ClientBuilder()
    .hosts(new HttpHosts(Constants.STREAM_HOST))
    .authentication(oAuth1)
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

  def iterate(size: Int, f: (String) => Any): Int = {
    var count = 0
    while ((!hosebirdClient.isDone) && !closingApp && count < size) {
      take() match {
        case Some(json) =>
          f
          count = count + 1
        case None =>
      }
    }
    count
  }

  def close = hosebirdClient.stop
}
