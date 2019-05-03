name := "StreamingProcessor"

version := "0.1"

scalaVersion := "2.12.8"


lazy val excludeJpountz = ExclusionRule(organization = "net.jpountz.lz4", name = "lz4")

libraryDependencies ++= Seq (
  "org.apache.spark" %% "spark-core" % "2.4.0",
  "org.apache.spark" %% "spark-sql" % "2.4.0",
  "org.apache.spark" %% "spark-streaming" % "2.4.0",
  //"org.apache.spark" %% "spark-streaming-kafka-0-8_2.11" % "2.4.0" excludeAll excludeJpountz,
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.2" excludeAll excludeJpountz,
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "org.json4s" %% "json4s-jackson" % "3.6.5",
  "com.softwaremill.sttp" %% "core" % "1.5.11",
  "com.typesafe.akka" %% "akka-actor" % "2.5.21",
  "com.typesafe.akka" %% "akka-stream" % "2.5.21",
  "com.typesafe.akka" %% "akka-http" % "10.1.7",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.21" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.7" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.21" % Test,
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7",
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0"
)