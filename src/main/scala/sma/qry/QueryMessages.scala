package sma.qry

import scala.concurrent.Future

object QueryMessages {

  case class Interests(interest: String) {

    def user = interest.split("@")(0)

    def network = interest.split("@")(1)

    def reply(topics: Future[Seq[String]]) = InterestReply(interest, topics)

    def mkString = s"topics request, follower: ${interest}"
  }

  case class InterestReply(interest: String, topics: Future[Seq[String]])
}
