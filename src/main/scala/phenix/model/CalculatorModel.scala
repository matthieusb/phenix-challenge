package phenix.model

import java.time.LocalDate

case class ProductSale(productId: Int, quantity: Int)
case class ProductTurnover(productId: Int, turnover: Double)

object ProductSaleOrdering extends Ordering[ProductSale] {
  override def compare(ps1: ProductSale, ps2: ProductSale): Int = ps1.quantity compare ps2.quantity
}

object ProductTurnoverOrdering extends Ordering[ProductTurnover] {
  override def compare(ps1: ProductTurnover, ps2: ProductTurnover): Int = ps1.turnover compare ps2.turnover
}


trait Shop {
  val shopUuid: String
}

case class DayShopSale(shopUuid: String, productSales: Stream[ProductSale]) extends Shop
case class DayGlobalSale(productSales: Stream[ProductSale])

case class DayShopTurnover(shopUuid: String, productTurnovers: Stream[ProductTurnover]) extends Shop
case class DayGlobalTurnover(productTurnovers: Stream[ProductTurnover])

case class CompleteDayKpi(date: LocalDate, dayShopSales: Stream[DayShopSale], dayGlobalSales: DayGlobalSale,
                          dayShopTurnovers: Stream[DayShopTurnover], dayGlobalTurnover: DayGlobalTurnover)