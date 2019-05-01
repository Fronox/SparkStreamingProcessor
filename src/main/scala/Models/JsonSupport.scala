package Models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.json4s.DefaultFormats
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._
  implicit val defaultFormat: DefaultFormats.type = DefaultFormats
  implicit val testDataJsonFormat: RootJsonFormat[TestData] = jsonFormat1(TestData)
  implicit val tuneDataJsonFormat: RootJsonFormat[TuneData] = jsonFormat2(TuneData)

}
