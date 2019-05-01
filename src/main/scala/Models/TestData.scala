package Models

case class TestData(date: String) extends ToJsonString {
  def toJsonString: String = {
    s"""{"date": "$date"}"""
  }
}