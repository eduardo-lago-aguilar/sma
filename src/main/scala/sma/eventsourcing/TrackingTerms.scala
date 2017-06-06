package sma.eventsourcing

case class TrackingTerms(terms: Seq[String]) {
  def mkString = terms.mkString(", ")
}