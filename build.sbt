name := "StreamingProcessor"

version := "0.1"

scalaVersion := "2.11.11"


lazy val excludeJpountz = ExclusionRule(organization = "net.jpountz.lz4", name = "lz4")

//enablePlugins(JavaAppPackaging)


mainClass in assembly := Some("org.fronox.diploma.runner.Main")

assemblyJarName in assembly := "task.jar"

assemblyMergeStrategy in assembly := {
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("META-INF", _) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

fullClasspath in assembly := (fullClasspath in Compile).value

val sparkVersion = "2.4.3"

libraryDependencies ++= Seq (
  "org.apache.spark" %% "spark-core" % sparkVersion/* % "provided"*/,
  "org.apache.spark" %% "spark-sql" % sparkVersion /*% "provided"*/,
  "org.apache.spark" %% "spark-streaming" % sparkVersion /*% "provided"*/,
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion excludeAll excludeJpountz,
  "org.json4s" %% "json4s-jackson" % "3.6.5",
  "com.softwaremill.sttp" %% "core" % "1.5.11",
  "com.typesafe.akka" %% "akka-actor" % "2.5.21",
  "com.typesafe.akka" %% "akka-stream" % "2.5.21",
  "com.typesafe.akka" %% "akka-http" % "10.1.7",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7",
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
)