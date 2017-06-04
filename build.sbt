val akkaHttpVersion = "10.0.7"
val akkaVersion = "2.5.2"
val akkaStreamKafkaVersion = "0.16"
val twitterVersion = "6.40.0"
val hbcCoreVersion = "2.2.0"
val jacksonScala = "2.8.4"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.2",
      resolvers ++= Seq(
        "Confluent" at "http://packages.confluent.io/maven/",
        "twttr" at "http://maven.twttr.com/"
      )
    )),
    name := "sma",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-kafka" % akkaStreamKafkaVersion,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonScala,
      "com.twitter" %% "util-core" % twitterVersion,
      "com.twitter" % "hbc-core" % hbcCoreVersion,

      "com.github.etaty" %% "rediscala" % "1.8.0",
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.1" % Test
    )
  )
