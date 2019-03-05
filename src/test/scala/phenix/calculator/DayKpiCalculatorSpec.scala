package phenix.calculator

import org.scalatest.{FlatSpec, Matchers}
import phenix.model.{ProductSale, _}

class DayKpiCalculatorSpec extends FlatSpec with Matchers with CalculatorSpec {

  "The Day Kpi Calculator" should "output correct sales results" in {
    // EXECUTE
    val dayKpiResult: CompleteDayKpi = DayKpiCalculator.computeDayKpi(transactions, List(productsOne, productsTwo).toStream)

    // ASSERT
    dayKpiResult.date shouldBe dayDate

    // -- Tests on shop 1
    val shop1ProductSales = dayKpiResult.dayShopSales
      .find(dayShopSale => dayShopSale.shopUuid == shopUuidOne)
      .map(dayShopSale => dayShopSale.productSales)

    shop1ProductSales shouldBe defined
    shop1ProductSales.get should have size 3
    shop1ProductSales.get should contain allOf (ProductSale(2, 15), ProductSale(1, 12), ProductSale(3, 3))

    // -- Tests on shop 2
    val shop2ProductSales = dayKpiResult.dayShopSales
      .find(dayShopSale => dayShopSale.shopUuid == shopUuidTwo)
      .map(dayShopSale => dayShopSale.productSales)

    shop2ProductSales shouldBe defined
    shop2ProductSales.get should have size 2
    shop2ProductSales.get should contain allOf (ProductSale(2, 16), ProductSale(3, 6))

    // -- Tests on global
    dayKpiResult.dayGlobalSales.productSales should contain allOf (ProductSale(2, 31), ProductSale(1, 12), ProductSale(3, 9))
  }

  "The Day Kpi Calculator" should "output correct turnover results" in {
    // EXECUTE
    val dayKpiResult: CompleteDayKpi = DayKpiCalculator.computeDayKpi(transactions, List(productsOne, productsTwo).toStream)

    // ASSERT
    dayKpiResult.date shouldBe dayDate

    // -- Tests on shop 1
    val shop1ProductTuronovers = dayKpiResult.dayShopTurnovers
      .find(dayShopTurnover => dayShopTurnover.shopUuid == shopUuidOne)
      .map(dayShopTurnover => dayShopTurnover.productTurnovers)

    shop1ProductTuronovers shouldBe defined
    shop1ProductTuronovers.get should have size 3
    shop1ProductTuronovers.get should contain allOf (ProductTurnover(1, 146.4), ProductTurnover(3, 120.0), ProductTurnover(2, 39))

    // -- Tests on shop 2
    val shop2ProductTurnovers = dayKpiResult.dayShopTurnovers
      .find(dayShopTurnover => dayShopTurnover.shopUuid == shopUuidTwo)
      .map(dayShopTurnover => dayShopTurnover.productTurnovers)

    shop2ProductTurnovers shouldBe defined
    shop2ProductTurnovers.get should have size 2
    shop2ProductTurnovers.get should contain allOf (ProductTurnover(3, 24.6), ProductTurnover(2, 198.4))

    // -- Tests on global
    dayKpiResult.dayGlobalTurnover.productTurnovers should contain allOf (ProductTurnover(2, 237.4), ProductTurnover(1, 146.4), ProductTurnover(3, 144.6))
  }

  "The Day Kpi calculator price getting method" should "get the right prices when they are present" in {
    // PREPARE
    val dayProducts = List(productsOne, productsTwo).toStream

    // EXECUTE
    val productOneShopOnePrice: Double = DayKpiCalculator.getProductPriceFromProducts(productIdOne, shopUuidOne, dayProducts)
    val productOneShopTwoPrice: Double = DayKpiCalculator.getProductPriceFromProducts(productIdOne, shopUuidTwo, dayProducts)
    val productTwoShopOnePrice: Double = DayKpiCalculator.getProductPriceFromProducts(productIdTwo, shopUuidOne, dayProducts)

    // ASSERT
    productOneShopOnePrice shouldBe 12.2
    productOneShopTwoPrice shouldBe 1.3
    productTwoShopOnePrice shouldBe 2.6
  }

  "The Day Kpi calculator price getting method" should "get 0.0 as price if the product is not found" in {
    // PREPARE
    val dayProducts = List(productsOne, productsTwo).toStream

    // EXECUTE
    val incorrectProcutShopOnePrice: Double = DayKpiCalculator.getProductPriceFromProducts(11122121, shopUuidOne, dayProducts)


    // ASSERT
    incorrectProcutShopOnePrice shouldBe 0.0
  }
}
