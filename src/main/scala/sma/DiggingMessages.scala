package sma

/**
  * Created by lago on 1/06/17.
  */
object DiggingMessages {
  abstract class Digging(follower: String, interest: String)

  case class Follow(follower: String, interest: String) extends Digging(follower: String, interest: String)

  case class FollowReply()

  case class Forget(follower: String, interest: String) extends Digging(follower: String, interest: String)

  case class ForgetReply()

}
