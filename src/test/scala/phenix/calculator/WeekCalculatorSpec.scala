package phenix.calculator

import org.scalatest.{FlatSpec, Matchers}
import phenix.model._

class WeekCalculatorSpec extends FlatSpec with Matchers with CalculatorSpec {
  val ShopSaleOne = ShopSale(shopUuidOne, Stream(ProductSale(2, 15), ProductSale(1, 12), ProductSale(3, 3)))
  val ShopSaleTwo = ShopSale(shopUuidTwo, Stream(ProductSale(2, 16), ProductSale(3, 6)))

  val shopTurnoverOne = ShopTurnover(shopUuidOne, Stream(ProductTurnover(1, 146.4), ProductTurnover(3, 120.0), ProductTurnover(2, 39)))
  val shopTurnoverTwo = ShopTurnover(shopUuidTwo, Stream(ProductTurnover(3, 24.6), ProductTurnover(2, 198.4)))

  val globalSale: GlobalSale = GlobalSale(Stream(ProductSale(2, 31), ProductSale(1, 12), ProductSale(3, 9)))
  val globalTurnover: GlobalTurnover = GlobalTurnover(Stream(ProductTurnover(2, 237.4), ProductTurnover(1, 146.4), ProductTurnover(3, 144.6)))

  val completeDayKpis: Stream[CompleteDayKpi] = (0 to 6).map(rangeVal => {
    CompleteDayKpi(dayDate.minusDays(rangeVal), Stream(ShopSaleOne, ShopSaleTwo), globalSale, Stream(shopTurnoverOne, shopTurnoverTwo), globalTurnover)
  }).toStream

  "The Week Kpi Calculator" should "output correct sales results" in {
    // EXECUTE
    val completeWeekKpi = WeekKpiCalculator.computeWeekKpi(dayDate, completeDayKpis)

    // ASSERT
    completeWeekKpi.lastDayDate shouldBe dayDate

    // -- Tests on shop 1
    val shop1WeekShopSales = completeWeekKpi.weekShopSales
      .find(shopSale => shopSale.shopUuid == shopUuidOne)
      .map(shopSale => shopSale.productSales)

    shop1WeekShopSales shouldBe defined
    shop1WeekShopSales.get should contain allOf(ProductSale(2, 105), ProductSale(1, 84), ProductSale(3, 21))

    // -- Tests on shop 2
    val shop2WeekShopSales = completeWeekKpi.weekShopSales
      .find(shopSale => shopSale.shopUuid == shopUuidTwo)
      .map(shopSale => shopSale.productSales)

    shop2WeekShopSales shouldBe defined
    shop2WeekShopSales.get should contain allOf(ProductSale(2, 112), ProductSale(3, 42))

    // Tests on Global
    completeWeekKpi.weekGlobalSales.productSales should contain allOf(ProductSale(2, 217), ProductSale(1, 84), ProductSale(3, 63))
  }

  "The Week Kpi Calculator" should "output correct turnover results" in {
    // EXECUTE
    val completeWeekKpi = WeekKpiCalculator.computeWeekKpi(dayDate, completeDayKpis)

    // ASSERT
    completeWeekKpi.lastDayDate shouldBe dayDate

    // -- Tests on shop 1
    val shop1WeekShopTurnovers = completeWeekKpi.weekShopTurnover
      .find(shopTurnover => shopTurnover.shopUuid == shopUuidOne)
      .map(shopTurnover => shopTurnover.productTurnovers)

    shop1WeekShopTurnovers shouldBe defined
    shop1WeekShopTurnovers.get should contain allOf(ProductTurnover(2, 273.0), ProductTurnover(1, 1024.8), ProductTurnover(3, 840.0))

    // -- Tests on shop 2
    val shop2WeekShopTurnovers = completeWeekKpi.weekShopTurnover
      .find(shopTurnover => shopTurnover.shopUuid == shopUuidTwo)
      .map(shopTurnover => shopTurnover.productTurnovers)

    shop2WeekShopTurnovers shouldBe defined
    shop2WeekShopTurnovers.get should contain allOf(ProductTurnover(2,1388.8), ProductTurnover(3,172.2))

    // Tests on Global
    completeWeekKpi.weekGlobalTurnover.productTurnovers should contain allOf(ProductTurnover(2,1661.8), ProductTurnover(1,1024.8), ProductTurnover(3,1012.2))
  }
}
