package org.fronox.diploma.models

case class PredictData(date: String, open: Double, high: Double, low: Double, close: Double) extends ToJsonString {
  def toJsonString: String = {
    s"""{"date": "$date", "open": "$open",  "high": "$high", "low": "$low", "close": "$close"}"""
  }
}