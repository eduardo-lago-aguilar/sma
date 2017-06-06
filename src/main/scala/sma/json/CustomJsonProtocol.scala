package sma.json;

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import sma.twitter.{TrackingTerm, Tweet}
import spray.json.DefaultJsonProtocol

object CustomJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val tweetFormat = jsonFormat4(Tweet.apply)
    implicit val trackingTermFormat = jsonFormat1(TrackingTerm.apply)
}
