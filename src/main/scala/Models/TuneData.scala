package Models

case class TuneData(date: String, quantity: Int, open: Double, high: Double, low: Double, close: Double) extends ToJsonString {
  def toJsonString: String = {
    s"""{"date": "$date", "quantity": $quantity, "open": "$open",  "high": "$high", "low": "$low", "close": "$close"}"""
  }
}