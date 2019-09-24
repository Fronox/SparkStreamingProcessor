package org.fronox.diploma.settings

import org.apache.kafka.common.serialization.StringDeserializer

case class Settings(kafkaIp: String, predictBaseIp: String, tuneBaseIp: String) {
  //Kafka settings
  val quorum: String = "localhost:2181"
  val groupId: String = "console-consumer-93845"
  val predictTopics = Array("media_markt_t1", "media_markt_t2", "media_markt_t6", "stream4", "stream5")
  val tuneTopics = predictTopics.map(x => x + "_tune")
  val kafkaParams: Map[String, Object] = Map[String, Object](
    "bootstrap.servers" -> s"$kafkaIp:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "fronox-spark-group_fn",
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )

  val basePredictPort = "5000"
  val baseTunePort = "5000"
  def toPredictEndpoint(end: Int): String = {
    s"http://$predictBaseIp$end:$basePredictPort/predict"
  }
  
  def toTuneEndpoint(end: Int): String = {
    s"http://$tuneBaseIp$end:$baseTunePort/tune"
  }
}
