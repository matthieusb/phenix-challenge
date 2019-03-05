package phenix.calculator


trait Calculator {
  /**
    * Round values to the 2 number after comma
    *
    * @param numberToRound the number you wish to round
    * @return
    */
  def roundValue(numberToRound: Double): Double = Math.round(numberToRound * 100.0) / 100.0
}
