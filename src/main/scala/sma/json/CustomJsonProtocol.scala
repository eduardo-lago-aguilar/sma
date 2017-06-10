package sma.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import sma.eventsourcing.User
import sma.twitter.TrackingTerm
import spray.json.DefaultJsonProtocol

object CustomJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val trackingTermFormat = jsonFormat1(TrackingTerm.apply)
  implicit val userFormat = jsonFormat1(User.apply)
}
