package Runner

import Models.{JsonSupport, TestData, ToJsonString, TuneData}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka._
import spray.json._
import org.json4s.jackson.Serialization.write

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends JsonSupport {
  //Spark settings
  val seconds: Int = 2
  val master: String = "local[*]"
  val appName: String = "Streaming processor"
  val conf: SparkConf = new SparkConf().setAppName(appName).setMaster(master)
  val ssc: StreamingContext = new StreamingContext(conf, Seconds(seconds))

  //Akka-http implicits
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  //implicit val formats = DefaultFormats

  //Kafka settings
  val quorum: String = "localhost:2181"
  val groupId: String = "console-consumer-93845"
  val topics: Map[String, Int] = Map(
    "test" -> 1
  )

  //Model API settings
  val host: String = "localhost"
  val port: String = "5000"
  val tuneEndpoint: String = s"http://$host:$port/tune"
  val resetEndpoint: String = s"http://$host:$port/reset"
  val predictEndpoint: String = s"http://$host:$port/predict"

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

  def main(args: Array[String]): Unit = {
    //Settings for logging
    ssc.sparkContext.setLogLevel("ERROR")

    //Input stream
    val kstream = KafkaUtils.createStream(ssc, quorum, groupId, topics)

    //Transformed streams
    val lines: DStream[String] = kstream.map(x => x._2).filter(x => x != "")
    val splittedLines: DStream[Array[String]] = lines.map(x => x.split(" "))
    val testData: DStream[TestData] = splittedLines.map {
      case Array(date, _, _) =>
        TestData(date)
    }
    val tuneData: DStream[TuneData] = splittedLines.window(Minutes(1), Minutes(1)).map{
      case Array(date, article, quantity) =>
        TuneData(date, quantity.toInt)
    }

    testData.print()
    tuneData.print()

    //Sending data to ML model
    //sendDataToUrl(testData, predictEndpoint)
    sendDataToUrl(tuneData, tuneEndpoint)

    //Streaming run settings
    ssc.start()
    ssc.awaitTermination()

    //File output drafts
    /*
    testData.foreachRDD{
      rdd =>
      rdd.foreachPartition{ part =>
          val csv = CSVWriter.open(new File("test.csv"), append = false)
          part.foreach(arr => csv.writeRow(arr))
          csv.close()
      }
    }*/

    /*trainData.map(x => {
      val file = new File("train.csv")
      val writer = new PrintWriter(file)
      writer.print("")
      writer.close()
      x
    }).*/

    /*val file = new File("train.csv")

    trainData.foreachRDD{ rdd =>
      rdd.foreachPartition { part =>
        val csv = CSVWriter.open(file, append = true)
        part.foreach(arr => csv.writeRow(arr))
        csv.close()
      }
    }*/
  }
}
