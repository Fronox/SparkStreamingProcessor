package Logic

import Models.{PredictData, TuneData}
import Settings.Settings._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

import scala.concurrent.ExecutionContext.Implicits.global

object Logic {
  //Akka-http implicits
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val defaultFormat: DefaultFormats.type = DefaultFormats

  //Data sending functions
  def sendPredictDataToUrl(streamData: DStream[(Int, PredictData)], uriStr: String): Unit = {
    streamData.foreachRDD{
      rdd =>
        rdd.groupByKey().foreach {
          case (portEnd: Int, arr: Iterable[PredictData]) =>
            val dataJson = write(arr)
            val uri = uriStr.replace("_", portEnd.toString)

            val request = HttpRequest (
              method = HttpMethods.POST,
              uri = Uri(uri),
              entity = HttpEntity(ContentTypes.`application/json`, dataJson)
            )
            Http().singleRequest(request).map(x => x.discardEntityBytes())
          }
    }
  }

  def sendTuneDataToUrl(streamData: DStream[(Int, TuneData)], uriStr: String): Unit = {
    streamData.foreachRDD{
      rdd =>
        rdd.groupByKey().foreach {
          case (portEnd: Int, arr: Iterable[TuneData]) =>
            val dataJson = write(arr)
            println(s"data $portEnd")
            println(dataJson)
            println()
            val uri = uriStr.replace("_", portEnd.toString)

            val request = HttpRequest (
              method = HttpMethods.POST,
              uri = Uri(uri),
              entity = HttpEntity(ContentTypes.`application/json`, dataJson)
            )
            Http().singleRequest(request).map(x => x.discardEntityBytes())
        }
    }
  }

  //Stream transformation functions:
  def inputStreamToLines(stream: InputDStream[ConsumerRecord[String, String]]): DStream[(Int, String)] = {
    stream
      .map(x => (x.key().toInt, x.value()))
      .filter(x => x._2 != "")
  }

  def streamSplit(stream: DStream[(Int, String)], stringSeparator: String): DStream[(Int, Array[String])] = {
    stream
      .mapValues(str => str.split(stringSeparator))
  }

  def streamToPredictData(stream: DStream[(Int, Array[String])]): DStream[(Int, PredictData)] = {
    stream.mapValues {
      case Array(date, open, high, low, close) =>
        PredictData(date, open.toDouble, high.toDouble, low.toDouble, close.toDouble)
    }
  }

  def streamToTuneData(stream: DStream[(Int, Array[String])], duration: streaming.Duration): DStream[(Int, TuneData)] = {
    stream.window(duration, duration).mapValues {
      case Array(date, open, high, low, close, quantity) =>
        TuneData(date, quantity.toInt, open.toDouble, high.toDouble, low.toDouble, close.toDouble)
    }
  }

  //Main job function
  def job(ssc: StreamingContext, predictTopics: Array[String], tuneTopics: Array[String],
          predictEndpoint: String, tuneEndpoint: String): Unit = {
    //Input streams
    val tuneStream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](tuneTopics, kafkaParams)
    )

    val predictStream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](predictTopics, kafkaParams)
    )

    //Transformed streams
    val tuneLines: DStream[(Int, String)] = inputStreamToLines(tuneStream)
    val predictLines: DStream[(Int, String)] = inputStreamToLines(predictStream)

    val tuneSplittedLines: DStream[(Int, Array[String])] = streamSplit(tuneLines, " ")
    val predictSplittedLines: DStream[(Int, Array[String])] = streamSplit(predictLines, " ")

    //Data for predict and tune
    val tuneData: DStream[(Int, TuneData)] = streamToTuneData(tuneSplittedLines, Seconds(4))
    val predictData: DStream[(Int, PredictData)] = streamToPredictData(predictSplittedLines)

    //Sending data serving layer
    sendPredictDataToUrl(predictData, predictEndpoint)
    sendTuneDataToUrl(tuneData, tuneEndpoint)
  }
}

/*Marshal(data.head).to[RequestEntity] flatMap { dataEntity =>
                val request = HttpRequest(
                  method = HttpMethods.POST,
                  uri = Uri(uriStr),
                  entity = dataEntity
                )
                println(dataEntity)
                Http().singleRequest(request)
              }*/
