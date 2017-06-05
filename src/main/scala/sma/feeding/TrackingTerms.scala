package sma.feeding

import sma.eventsourcing.Topics

case class RetrieveTrackingTerms(userAtNetwork: String) extends Topics {

  def user = splittingUserAt(userAtNetwork)(0)

  def network = splittingUserAt(userAtNetwork)(1)

  def mkString = s"topics request, follower: ${userAtNetwork}"
}

case class TrackingTerms(terms: Seq[String]) {
  def mkString = terms.mkString(", ")
}