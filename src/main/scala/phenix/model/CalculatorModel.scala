package phenix.model

import java.util.Date

case class ProductSale(productId: Int, quantity: Int)
case class ProductTurnover(productId: Int, turnover: Double)

trait Day {
  val date: Date
}

trait DayShop extends Day {
  val shopUuid: String
}

case class DayShopSale(date: Date, shopUuid: String, productSales: Stream[ProductSale]) extends DayShop
case class DayGlobalSale(date: Date, productSales: Stream[ProductSale]) extends Day

case class DayShopTurnover(date: Date, shopUuid: String, productTurnovers: Stream[ProductTurnover]) extends DayShop
case class DayGlobalTurnover(date: Date, productTurnovers: Stream[ProductTurnover]) extends Day

case class CompleteDayKpi(dayShopSales: Stream[DayShopSale], dayGlobalSales: DayGlobalSale,
                          dayShopTurnovers: Stream[DayShopTurnover], dayGlobalTurnover: DayGlobalTurnover)