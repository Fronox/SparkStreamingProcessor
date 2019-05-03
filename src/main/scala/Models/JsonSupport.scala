package Models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.json4s.DefaultFormats
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._
  implicit val defaultFormat: DefaultFormats.type = DefaultFormats
  /*implicit val testDataJsonFormat: RootJsonFormat[PredictData] = jsonFormat5(PredictData)
  implicit val tuneDataJsonFormat: RootJsonFormat[TuneData] = jsonFormat6(TuneData)*/
}
