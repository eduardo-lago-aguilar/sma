package sma.json

import java.lang.reflect.{ParameterizedType, Type}

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.{UnrecognizedPropertyException => UPE}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object Json {

  type ParseException = JsonParseException
  type UnrecognizedPropertyException = UPE

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

  private def typeReference[T: Manifest] = new TypeReference[T] {
    override def getType = typeFromManifest(manifest[T])
  }

  private def typeFromManifest(m: Manifest[_]): Type = {
    if (m.typeArguments.isEmpty) {
      m.runtimeClass
    }
    else new ParameterizedType {
      def getRawType = m.runtimeClass

      def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray

      def getOwnerType = null
    }
  }

  def encode(value: Any): Array[Byte] = mapper.writeValueAsBytes(value)

  def decode[T: Manifest](value: Array[Byte]): T = mapper.readValue(value, typeReference[T])

  def decode[T: Manifest](value: String): T = mapper.readValue(value, typeReference[T])

  val prettyPrinterWriter = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.writerWithDefaultPrettyPrinter
  }

  object Tweet {
    def decodeId(json: String): String = Json.decode[Map[String, Any]](json).get("id_str").get.toString
  }

}
