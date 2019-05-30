package Runner

import Logic.Logic._
import Settings.Settings._
import org.apache.spark._
import org.apache.spark.streaming._

object Main {
  //Spark settings
  val seconds: Duration = Seconds(1)
  val master: String = "local[*]"
  val appName: String = "Streaming processor"
  val conf: SparkConf = new SparkConf().setAppName(appName).setMaster(master).set("spark.streaming.concurrentJobs", "2")
    .set("spark.scheduler.mode", "FAIR")
  val ssc: StreamingContext = new StreamingContext(conf, seconds)

  def main(args: Array[String]): Unit = {
    //Settings for logging
    ssc.sparkContext.setLogLevel("ERROR")

    //Jobs settings
    job(ssc, predictTopics1, tuneTopics1, predictTemp1, tuneTemp1)

    //Streaming run settings
    ssc.start()
    ssc.awaitTermination()
  }
}
