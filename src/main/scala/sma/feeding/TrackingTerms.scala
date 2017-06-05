package sma.feeding

case class RetrieveTrackingTerms(userAtNetwork: String) {

  def user = userAtNetwork.split("@")(0)

  def network = userAtNetwork.split("@")(1)

  def mkString = s"topics request, follower: ${userAtNetwork}"
}

case class TrackingTerms(terms: Seq[String]) {
  def mkString = terms.mkString(", ")
}