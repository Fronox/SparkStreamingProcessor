package Runner

import Models.{JsonSupport, PredictData, ToJsonString, TuneData}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.json4s.jackson.Serialization.write
import spray.json._

object Main extends JsonSupport {
  //Spark settings
  val seconds: Int = 1
  val master: String = "local[*]"
  val appName: String = "Streaming processor"
  val conf: SparkConf = new SparkConf().setAppName(appName).setMaster(master)
  val ssc: StreamingContext = new StreamingContext(conf, Seconds(seconds))

  //Akka-http implicits
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  //Kafka settings
  val quorum: String = "localhost:2181"
  val groupId: String = "console-consumer-93845"
  val topics1 = Array("media_markt_t1")
  val topics2 = Array("media_markt_t2")
  val topics3 = Array("media_markt_t6")
  val kafkaParams: Map[String, Object] = Map[String, Object](
    "bootstrap.servers" -> "localhost:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "fronox-spark-group",
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )

  //Model API settings
  val host: String = "localhost"
  val port1: String = "5000"
  val tuneEndpoint1: String = s"http://$host:$port1/tune"
  val resetEndpoint1: String = s"http://$host:$port1/reset"
  val predictEndpoint1: String = s"http://$host:$port1/predict"

  val port2: String = "idontknowyet"
  val tuneEndpoint2: String = s"http://$host:$port2/tune"
  val resetEndpoint2: String = s"http://$host:$port2/reset"
  val predictEndpoint2: String = s"http://$host:$port2/predict"

  val port3: String = "idontknowyet"
  val tuneEndpoint3: String = s"http://$host:$port3/tune"
  val resetEndpoint3: String = s"http://$host:$port3/reset"
  val predictEndpoint3: String = s"http://$host:$port3/predict"

  //Data sending function
  def sendDataToUrl[T <: ToJsonString](streamData: DStream[T], uriStr: String)(implicit format: RootJsonFormat[T]): Unit = {
    streamData.foreachRDD{
      rdd =>
        println(s"new rdd ${rdd.hashCode()}")
        val data: List[T] = rdd.collect().toList
        if(data.nonEmpty) {
          println("data:")
          data.foreach(println)
          val request = HttpRequest(
            method = HttpMethods.POST,
            uri = Uri(uriStr),
            entity = write(data)
          )

          Http().singleRequest(request)

          /*Marshal(data.head).to[RequestEntity] flatMap { dataEntity =>
            val request = HttpRequest(
              method = HttpMethods.POST,
              uri = Uri(uriStr),
              entity = dataEntity
            )
            println(dataEntity)
            Http().singleRequest(request)
          }*/
        }
    }
  }

  //Stream transformation functions:

  def inputStreamToLines(stream: InputDStream[ConsumerRecord[String, String]]): DStream[String] = {
    stream
      .map(x => x.value())
      .filter(x => x != "")
  }

  def streamToSplittedLines(stream: DStream[String], separator: String): DStream[Array[String]] = {
    stream.map(x => x.split(separator))
  }

  def streamToPredictData(stream: DStream[Array[String]]): DStream[PredictData] = {
    stream.map {
      case Array(date, _) =>
        PredictData(date)
    }
  }

  def streamToTuneData(stream: DStream[Array[String]], duration: Duration): DStream[TuneData] = {
    stream.window(duration, duration).map{
      case Array(date, quantity) =>
        TuneData(date, quantity.toInt)
    }
  }

  //Main job function
  def job(topics: Array[String], predictEndpoint: String, tuneEndpoint: String, resetEndpoint: String = ""): Unit = {
    //Input stream
    val kstream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    //Transformed streams
    val lines: DStream[String] = inputStreamToLines(kstream)

    val splittedLines: DStream[Array[String]] = streamToSplittedLines(lines, " ")

    //Data for predict and tune
    val predictData: DStream[PredictData] = streamToPredictData(splittedLines)
    val tuneData: DStream[TuneData] = streamToTuneData(splittedLines, Seconds(10))

    predictData.print()
    tuneData.print()

    //Sending data to ML model
    //sendDataToUrl(predictData, predictEndpoint)
    //sendDataToUrl(tuneData, tuneEndpoint)
  }

  def main(args: Array[String]): Unit = {
    //Settings for logging
    ssc.sparkContext.setLogLevel("ERROR")

    job(topics1, predictEndpoint1, tuneEndpoint1, resetEndpoint1)

    //Streaming run settings
    ssc.start()
    ssc.awaitTermination()
  }
}
