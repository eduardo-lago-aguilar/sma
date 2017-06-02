val akkaHttpVersion = "10.0.7"
val akkaVersion    = "2.5.2"
val akkaStreamKafkaVersion = "0.16"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.2",
      resolvers += "Confluent" at "http://packages.confluent.io/maven/"
    )),
    name := "sma",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"       % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-kafka" % akkaStreamKafkaVersion,
      "com.sksamuel.avro4s" %% "avro4s-core" % "1.6.4",
      "org.apache.avro" % "avro" % "1.7.7",
      "io.confluent" % "kafka-avro-serializer" % "1.0",
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "org.scalatest"     %% "scalatest"         % "3.0.1"         % Test
    )
  )
