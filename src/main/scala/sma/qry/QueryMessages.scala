package sma.qry

object QueryMessages {

  case class Topics(follower: String) {

    def reply(topics: Seq[String]) = TopicsReply(follower, topics)

    def mkString = s"topics request, follower: ${follower}"
  }

  case class TopicsReply(follower: String, topics: Seq[String]) {
    def mkString = topics.mkString
  }

}
