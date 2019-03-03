package phenix.model

import java.time.LocalDate

case class ProductSale(productId: Int, quantity: Int) extends Ordered[ProductSale] {
  override def compare(ps2: ProductSale): Int = this.quantity compare ps2.quantity
}
case class ProductTurnover(productId: Int, turnover: Double) extends Ordered[ProductTurnover] {
  override def compare(pt2: ProductTurnover): Int = this.turnover compare pt2.turnover
}

abstract class Shop {
  val shopUuid: String
}

case class DayShopSale(shopUuid: String, productSales: Stream[ProductSale]) extends Shop
case class DayGlobalSale(productSales: Stream[ProductSale])

case class DayShopTurnover(shopUuid: String, productTurnovers: Stream[ProductTurnover]) extends Shop
case class DayGlobalTurnover(productTurnovers: Stream[ProductTurnover])

case class CompleteDayKpi(date: LocalDate, dayShopSales: Stream[DayShopSale], dayGlobalSales: DayGlobalSale,
                          dayShopTurnovers: Stream[DayShopTurnover], dayGlobalTurnover: DayGlobalTurnover)

object CompleteDayKpi {
  def sortDayKpiResults(completeDayKpi: CompleteDayKpi): CompleteDayKpi = {
    completeDayKpi.copy(
    dayShopSales = completeDayKpi.dayShopSales.map(dayShopSale => {
      dayShopSale.copy(productSales = dayShopSale.productSales.sorted.reverse)
    }),
    dayGlobalSales = completeDayKpi.dayGlobalSales.copy(productSales = completeDayKpi.dayGlobalSales.productSales.sorted.reverse),

    dayShopTurnovers = completeDayKpi.dayShopTurnovers.map(dayShopTurnover => {
      dayShopTurnover.copy(productTurnovers = dayShopTurnover.productTurnovers.sorted.reverse)
    }),
    dayGlobalTurnover = completeDayKpi.dayGlobalTurnover.copy(productTurnovers = completeDayKpi.dayGlobalTurnover.productTurnovers.sorted.reverse)
    )
  }
}

case class RangeDaykpi(dayKpis: Stream[CompleteDayKpi])
