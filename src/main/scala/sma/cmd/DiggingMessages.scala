package sma.cmd

import java.io.ByteArrayOutputStream

import com.sksamuel.avro4s.AvroSchema
import org.apache.avro.generic.IndexedRecord
import org.apache.avro.io.{BinaryEncoder, EncoderFactory}
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.common.utils.Bytes.wrap
import sma.BytesSerializableMessage

object DiggingMessages {

  abstract class Digging(val follower: String, interest: String) extends IndexedRecord with BytesSerializableMessage {

    def followee = interest.split("@")(0)

    def media = interest.split("@")(1)

    def reply: DiggingReply

    def mkString: String

    def serialize: Bytes = {
      val out = new ByteArrayOutputStream()
      val encoder = EncoderFactory.get.binaryEncoder(out, null)
      write(encoder)
      encoder.flush
      out.close
      wrap(out.toByteArray)
    }

    override def get(i: Int) = {
      i match {
        case 0 => follower
        case 1 => interest
      }
    }

    override def put(i: Int, v: scala.Any) = {
      // TODO: !
    }

    protected def write(encoder: BinaryEncoder): Unit

  }

  abstract class DiggingReply

  case class Follow(override val follower: String, interest: String) extends Digging(follower: String, interest: String) {

    override def reply = FollowReply()

    override def mkString = s"FOLLOW request, follower: ${follower}, interest: ${interest}"

    override def getSchema = AvroSchema[Follow]

    protected override def write(encoder: BinaryEncoder): Unit = {
      val writer = new SpecificDatumWriter[Follow](getSchema)
      writer.write(this, encoder)
    }
  }

  case class FollowReply() extends DiggingReply

  case class Forget(override val follower: String, interest: String) extends Digging(follower: String, interest: String) {
    override def reply = ForgetReply()

    override def mkString = s"FORGET request, follower: ${follower}, interest: ${interest}"

    override def getSchema = AvroSchema[Forget]

    protected override def write(encoder: BinaryEncoder): Unit = {
      val writer = new SpecificDatumWriter[Forget](getSchema)
      writer.write(this, encoder)
    }
  }

  case class ForgetReply() extends DiggingReply

}
