package phenix.model

import java.time.LocalDate

case class ProductSale(productId: Int, quantity: Int)
case class ProductTurnover(productId: Int, turnover: Double)

trait Day {
  val date: LocalDate
}

trait DayShop extends Day {
  val shopUuid: String
}

case class DayShopSale(date: LocalDate, shopUuid: String, productSales: Stream[ProductSale]) extends DayShop
case class DayGlobalSale(date: LocalDate, productSales: Stream[ProductSale]) extends Day

case class DayShopTurnover(date: LocalDate, shopUuid: String, productTurnovers: Stream[ProductTurnover]) extends DayShop
case class DayGlobalTurnover(date: LocalDate, productTurnovers: Stream[ProductTurnover]) extends Day

case class CompleteDayKpi(dayShopSales: Stream[DayShopSale], dayGlobalSales: DayGlobalSale,
                          dayShopTurnovers: Stream[DayShopTurnover], dayGlobalTurnover: DayGlobalTurnover)