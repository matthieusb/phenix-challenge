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

case class ShopSale(shopUuid: String, productSales: Stream[ProductSale]) extends Shop {
  def sort(): ShopSale = {
    this.copy(productSales = this.productSales.sorted.reverse)
  }
}

case class GlobalSale(productSales: Stream[ProductSale]) {
  def sort(): GlobalSale = {
    this.copy(productSales = this.productSales.sorted.reverse)
  }
}

case class ShopTurnover(shopUuid: String, productTurnovers: Stream[ProductTurnover]) extends Shop {
  def sort(): ShopTurnover = {
    this.copy(productTurnovers = this.productTurnovers.sorted.reverse)
  }
}

case class GlobalTurnover(productTurnovers: Stream[ProductTurnover]) {
  def sort(): GlobalTurnover = {
    this.copy(productTurnovers = this.productTurnovers.sorted.reverse)
  }
}

case class CompleteDayKpi(date: LocalDate, dayShopSales: Stream[ShopSale], dayGlobalSales: GlobalSale,
                          dayShopTurnovers: Stream[ShopTurnover], dayGlobalTurnover: GlobalTurnover) {
  def sortResults(): CompleteDayKpi = {
    this.copy(
      dayShopSales = this.dayShopSales.map(_.sort()),
      dayGlobalSales = this.dayGlobalSales.sort(),
      dayShopTurnovers = this.dayShopTurnovers.map(_.sort()),
      dayGlobalTurnover = this.dayGlobalTurnover.sort()
    )
  }

  // TODO Add the truncate method here
}

case class WeekKpi(lastDayDate: LocalDate, weekShopSales: Stream[ShopSale], weekGlobalSales: GlobalSale,
                   weekShopTurnover: Stream[ShopTurnover], weekGlobalTurnover: GlobalTurnover) {
  def sortResults(): WeekKpi = {
    this.copy(
      weekShopSales = this.weekShopSales.map(_.sort()),
      weekGlobalSales = this.weekGlobalSales.sort(),
      weekShopTurnover = this.weekShopTurnover.map(_.sort()),
      weekGlobalTurnover = this.weekGlobalTurnover.sort()
    )
  }

  // TODO Add truncate method
}