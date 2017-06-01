package sma

/**
  * Created by lago on 1/06/17.
  */
object DiggingMessages {

  val defaultMedia = "twitter"

  abstract class Digging(follower: String, interest: String) {

    private val _interest = s"${interest}${defaultMedia}".split("@")

    val followee = _interest(0)
    def media = _interest(1)

    def repack: Digging
    def reply: DiggingReply
    def mkString: String
  }

  abstract class DiggingReply

  case class Follow(follower: String, interest: String) extends Digging(follower: String, interest: String) {
    override def repack = {
      Follow(follower, followee)
    }

    override def reply = FollowReply()

    override def mkString = s"follow request, follower: ${follower}, interest: ${interest}"
  }

  case class FollowReply() extends DiggingReply

  case class Forget(follower: String, interest: String) extends Digging(follower: String, interest: String) {
    override def repack = {
      Forget(follower, followee)
    }

    override def reply = ForgetReply()

    override def mkString = s"forget request, follower: ${follower}, interest: ${interest}"
  }

  case class ForgetReply() extends DiggingReply

}
