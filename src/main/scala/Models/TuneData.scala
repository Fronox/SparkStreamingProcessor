package Models

case class TuneData(date: String, quantity: Int) extends ToJsonString {
  def toJsonString: String = {
    s"""{"date": "$date", "quantity": $quantity}"""
  }
}