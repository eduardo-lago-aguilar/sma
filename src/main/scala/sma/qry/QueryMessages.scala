package sma.qry

import sma.EventSourcing

import scala.concurrent.Future

object QueryMessages {

  case class Interests(interest: String) {

    def user = interest.split("@")(0)

    def network = interest.split("@")(1)

    def reply(topics: Future[Seq[String]]) = InterestReply(interest, topics)

    def mkString = s"topics request, follower: ${interest}"
  }

  case class InterestReply(interest: String, topics: Future[Seq[String]]) extends EventSourcing {
    def mkString = {
      var str = ""
      topics.foreach(topics => {
        val t = topics.reduce((total, current) => s"${total}, ${current}")
        str = s"${t}, ${str}"
      })
      str
    }
  }

}
