package Models

case class PredictData(date: String) extends ToJsonString {
  def toJsonString: String = {
    s"""{"date": "$date"}"""
  }
}