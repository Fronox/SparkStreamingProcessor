package org.fronox.diploma.runner

import org.apache.spark._
import org.apache.spark.streaming._
import org.fronox.diploma.logic.Logic._
import org.fronox.diploma.settings.Settings

object Main {
  //Spark settings
  val seconds: Duration = Seconds(1)
  val master: String = "local[*]" //"spark://192.168.0.7:7077"
  val appName: String = "Streaming processor"
  val conf: SparkConf = new SparkConf()/*.setAppName(appName).setMaster(master)*/.set("spark.streaming.concurrentJobs", "2")
  //.set("spark.executor.cores", "1")//
    //.set("spark.scheduler.mode", "FAIR")
  val ssc: StreamingContext = new StreamingContext(conf, seconds)

  def main(args: Array[String]): Unit = {
    //Settings for logging
    ssc.sparkContext.setLogLevel("ERROR")
    val kafkaIp = args(0)
    val predictBaseIp= args(1)
    val tuneBaseIp= args(2)
    //Jobs settings
    job(ssc, Settings(kafkaIp, predictBaseIp, tuneBaseIp))

    //Streaming run settings
    ssc.start()
    ssc.awaitTermination()
  }
}
