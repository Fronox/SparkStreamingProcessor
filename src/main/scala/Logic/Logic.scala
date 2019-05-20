package Logic

import Models.{PredictData, ToJsonString, TuneData}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, Uri}
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.json4s.jackson.Serialization.write
import Settings.Settings._
import org.json4s.DefaultFormats

object Logic {
  //Akka-http implicits
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val defaultFormat: DefaultFormats.type = DefaultFormats

  //Data sending function
  def sendDataToUrl[T <: ToJsonString](streamData: DStream[T], uriStr: String)/*(implicit format: RootJsonFormat[T])*/: Unit = {
    streamData.foreachRDD{
      rdd =>
        if(!rdd.isEmpty()) {
          val data: List[T] = rdd.collect().toList
          println("data:")
          println(data)
          val request = HttpRequest (
            method = HttpMethods.POST,
            uri = Uri(uriStr),
            entity = HttpEntity(ContentTypes.`application/json`, write(data))
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

  def streamSplit(stream: DStream[String], stringSeparator: String): DStream[Array[String]] = {
    stream
      .map(str => str.split(stringSeparator))
  }

  def streamToPredictData(stream: DStream[Array[String]]): DStream[PredictData] = {
    stream.map {
      case Array(date, open, high, low, close) =>
        PredictData(date, open.toDouble, high.toDouble, low.toDouble, close.toDouble)
    }
  }

  def streamToTuneData(stream: DStream[Array[String]], duration: streaming.Duration): DStream[TuneData] = {
    stream.window(duration, duration).map{
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
    val tuneLines: DStream[String] = inputStreamToLines(tuneStream)
    val predictLines: DStream[String] = inputStreamToLines(predictStream)

    val tuneSplittedLines: DStream[Array[String]] = streamSplit(tuneLines, " ")
    val predictSplittedLines: DStream[Array[String]] = streamSplit(predictLines, " ")

    //Data for predict and tune
    val tuneData: DStream[TuneData] = streamToTuneData(tuneSplittedLines, Seconds(4))
    val predictData: DStream[PredictData] = streamToPredictData(predictSplittedLines)

    /*tuneData.print()
    predictData.print()*/

    //Sending data serving layer
    sendDataToUrl(predictData, predictEndpoint)
    sendDataToUrl(tuneData, tuneEndpoint)
  }
}
