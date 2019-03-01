package phenix.calculator

import java.time.LocalDate

import com.typesafe.scalalogging.LazyLogging
import phenix.model._

/**
  * Does the calculations to get indicators (sales, turnover) according to cases (one day, last 7 days).
  */
object IndicatorCalculator extends LazyLogging {


  /**
    * TODO Documentation
    *
    * WARNING: this does not handle sorting. The values here are streams and not ordered, you'll have to do it before you output them to files.
    *
    * @param dayTransactions
    * @param dayProductsList
    * @return
    */
  def computeDayKpi(dayTransactions: Transactions, dayProductsList: Stream[Products]): CompleteDayKpi = {
    logger.info(s"Lancement des calculs des KPI par jour. Date: ${dayTransactions.metaData.date}")

    val productKpiMapByShop = computeProductDayKpiByShop(dayTransactions, dayProductsList)

    val dayShopSales = productKpiMapByShop.keys.map(shopUuid => {
      val productSaleExtract = productKpiMapByShop(shopUuid).map(productDataTuple => productDataTuple._1)
      DayShopSale(dayTransactions.metaData.date, shopUuid, productSaleExtract)
    }).toStream

    val dayShopTurnovers = productKpiMapByShop.keys.map(shopUuid => {
      val productSaleExtract = productKpiMapByShop(shopUuid).map(productDataTuple => productDataTuple._2)
      DayShopTurnover(dayTransactions.metaData.date, shopUuid, productSaleExtract)
    }).toStream

    val dayGlobalSale = computeGlobalDaySales(dayTransactions.metaData.date, dayShopSales)
    val dayGlobalTurnover = computeGlobalDayTurnover(dayTransactions.metaData.date, dayShopTurnovers)

    CompleteDayKpi(dayShopSales, dayGlobalSale, dayShopTurnovers, dayGlobalTurnover)
  }

  def computeGlobalDaySales(date: LocalDate, dayShopSales: Stream[DayShopSale]): DayGlobalSale = {
    logger.info(s"Calcul du nombre de ventes global pour la date $date")

    val aggregatedProductSales = dayShopSales.flatMap(dayShopSale => {
      dayShopSale.productSales
    })
      .groupBy(productSale => productSale.productId)
      .mapValues(productSale => {
        productSale.foldLeft(0)((acc, productSale2) => acc + productSale2.quantity)
      }).map(globalResultMap => {
      ProductSale(globalResultMap._1, globalResultMap._2)
    })

    DayGlobalSale(date, aggregatedProductSales.toStream)
  }

  def computeGlobalDayTurnover(date: LocalDate, dayShopTurnovers: Stream[DayShopTurnover]): DayGlobalTurnover = {
    logger.info(s"Calcul du CA global pour la date $date")

    val aggregateProductTurnovers = dayShopTurnovers.flatMap(dayShopTurnover => {
      dayShopTurnover.productTurnovers
    })
      .groupBy(productTurnover => productTurnover.productId)
      .mapValues(productTurnover => {
        productTurnover.foldLeft(0.0)((acc, productTurnover2) => acc + productTurnover2.turnover)
      }).map(globalResultMap => {
      ProductTurnover(globalResultMap._1, globalResultMap._2)
    })

    DayGlobalTurnover(date, aggregateProductTurnovers.toStream)
  }

  /**
    * TODO Documentation
    * @param dayTransactions
    * @param dayProductsList
    * @return
    */
  def computeProductDayKpiByShop(dayTransactions: Transactions, dayProductsList: Stream[Products]): Map[String, Stream[(ProductSale, ProductTurnover)]] = {
    logger.info(s"Calcul des kpi (Ventes, CA) pour la date ${dayTransactions.metaData.date}")

    dayTransactions.transactions
      .groupBy(transaction => (transaction.shopUuid, transaction.productId))
      .mapValues(transactions => {
        transactions.foldLeft(0)((acc, transaction2) => acc + transaction2.quantity)
      })
      .map(transactionMap => {
        val productId = transactionMap._1._2
        val shopUuid = transactionMap._1._1
        val productQuantity = transactionMap._2
        val productTotalPrice = productQuantity * getProductPriceFromProducts(productId, shopUuid, dayProductsList)
        (shopUuid, productId, productQuantity, productTotalPrice)
      })
      .groupBy(calculationResult => calculationResult._1) // group by shop uuid
      .map(shopProductCalc => { // remove uuid field from all parts of the list and turn them into case classes
      val shopProductsCalcValues = shopProductCalc._2.toStream
      shopProductCalc._1 -> shopProductsCalcValues.map(productAggregateCalc => (ProductSale(productAggregateCalc._2, productAggregateCalc._3), ProductTurnover(productAggregateCalc._2, roundValue(productAggregateCalc._4)))) // Rounder to 2 decimals
    })
  }

  def roundValue(numberToRound: Double): Double = Math.round(numberToRound * 100.0) / 100.0

  /**
    * TODO DOcumentation
    *
    * FIXME Some error handling could be done here
    * @param productId
    * @param shopUuid
    * @param dayProductsList
    * @return
    */
  def getProductPriceFromProducts(productId: Int, shopUuid: String, dayProductsList: Stream[Products]): Double = {
    dayProductsList
      .find(dayProducts => dayProducts.metaData.shopUuid == shopUuid)
      .get.products.find(product => product.productId == productId)
      .get.price
  }

}
