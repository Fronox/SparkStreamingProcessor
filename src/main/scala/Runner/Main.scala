package Runner

import org.apache.spark._
import org.apache.spark.streaming._
import Settings.Settings._
import Logic.Logic._

object Main {
  //Spark settings
  val seconds: Int = 1
  val master: String = "local[*]"
  val appName: String = "Streaming processor"
  val conf: SparkConf = new SparkConf().setAppName(appName).setMaster(master)
  val ssc: StreamingContext = new StreamingContext(conf, Seconds(seconds))

  def main(args: Array[String]): Unit = {
    //Settings for logging
    ssc.sparkContext.setLogLevel("ERROR")

    //Jobs settings
    job(ssc, predictTopics1, tuneTopics1, predictEndpoint1, tuneEndpoint1)

    //Streaming run settings
    ssc.start()
    ssc.awaitTermination()
  }
}
