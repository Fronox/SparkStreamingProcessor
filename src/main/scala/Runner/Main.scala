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
  val conf: SparkConf = new SparkConf().setAppName(appName).setMaster(master).set("spark.streaming.concurrentJobs", "6")
    .set("spark.scheduler.mode", "FAIR")
  val ssc: StreamingContext = new StreamingContext(conf, Seconds(seconds))

  def main(args: Array[String]): Unit = {
    //Settings for logging
    ssc.sparkContext.setLogLevel("ERROR")

    //Jobs settings
    job(ssc, predictTopics1, tuneTopics1, predictEndpoint1, tuneEndpoint1)
    //job(ssc, predictTopics2, tuneTopics2, predictEndpoint2, tuneEndpoint2)
    //job(ssc, predictTopics3, tuneTopics3, predictEndpoint3, tuneEndpoint3)

    //Streaming run settings
    ssc.start()
    ssc.awaitTermination()
  }
}
