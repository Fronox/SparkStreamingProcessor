package Settings

import org.apache.kafka.common.serialization.StringDeserializer

object Settings {
  //Kafka settings
  val quorum: String = "localhost:2181"
  val groupId: String = "console-consumer-93845"
  val predictTopics1 = Array("media_markt_t1")
  val predictTopics2 = Array("media_markt_t2")
  val predictTopics3 = Array("media_markt_t6")
  val tuneTopics1 = Array("media_markt_t1_tune")
  val tuneTopics2 = Array("media_markt_t2_tune")
  val tuneTopics3 = Array("media_markt_t6_tune")
  val kafkaParams: Map[String, Object] = Map[String, Object](
    "bootstrap.servers" -> "localhost:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "fronox-spark-group",
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )

  //Serving layer settings
  /*val host = "localhost"
  val port = "8000"
  val prefix = "servingLayer"
  val predictEndpoint = s"http://$host:$port/$prefix/predict/"
  val tuneEndpoint = s"http://$host:$port/$prefix/tune/"*/

  //Model API settings
  private val host: String = "localhost"

  private val port1: String = "5000"
  val tuneEndpoint1: String = s"http://$host:$port1/tune"
  val resetEndpoint1: String = s"http://$host:$port1/reset"
  val predictEndpoint1: String = s"http://$host:$port1/predict"

  private val port2: String = "idontknowyet"
  val tuneEndpoint2: String = s"http://$host:$port2/tune"
  val resetEndpoint2: String = s"http://$host:$port2/reset"
  val predictEndpoint2: String = s"http://$host:$port2/predict"

  val port3: String = "idontknowyet"
  val tuneEndpoint3: String = s"http://$host:$port3/tune"
  val resetEndpoint3: String = s"http://$host:$port3/reset"
  val predictEndpoint3: String = s"http://$host:$port3/predict"
}
