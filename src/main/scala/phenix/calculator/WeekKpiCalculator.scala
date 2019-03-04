package phenix.calculator

import java.time.LocalDate

import com.typesafe.scalalogging.LazyLogging
import phenix.model._

/**
  * Does the calculations to get indicators (sales, turnover) according to cases for one day.
  * Depends on per day calculations done by DayKpiCalculator, wich you can get sorted and truncated through Orchestrator.
  */
object WeekKpiCalculator extends LazyLogging with Calculator {
  /**
    * TODO Documentation
    *
    * @param weekCompleteDayKpis a Stream of only 7 CompleteDayKpis, should be filtered and sorted before so that the oldest date is a the top.
    */
  def computeWeekKpi(lastDayDate: LocalDate, weekCompleteDayKpis: Stream[CompleteDayKpi]): WeekKpi = {
    val allShopSales = weekCompleteDayKpis.flatMap(completeDayKpi =>  completeDayKpi.dayShopSales)
    val weekShopSales = computeWeekShopSales(allShopSales)

    val allGlobalSales = weekCompleteDayKpis.map(completeDayKpi => completeDayKpi.dayGlobalSales)
    val weekGlobalSales = computeWeekGlobalSales(allGlobalSales)

    val allShopTurnovers = weekCompleteDayKpis.flatMap(completeDayKpi =>  completeDayKpi.dayShopTurnovers)
    val weekShopTurnovers = computeWeekShopTurnovers(allShopTurnovers)

    val allGlobalTurnovers = weekCompleteDayKpis.map(completeDayKpi => completeDayKpi.dayGlobalTurnover)
    val weekGlobalTurnovers = computeWeekGlobalTurnovers(allGlobalTurnovers)

    WeekKpi(lastDayDate, weekShopSales, weekGlobalSales, weekShopTurnovers, weekGlobalTurnovers)
  }

  def computeWeekShopSales(allShopSales: Stream[ShopSale]): Stream[ShopSale] = {
    allShopSales
      .groupBy(shopSale => shopSale.shopUuid)
      .map(shopSaleByUuidMap => (shopSaleByUuidMap._1,
        shopSaleByUuidMap._2
          .flatMap(shopSale => shopSale.productSales)
          .groupBy(productSale => productSale.productId)
          .mapValues(groupedByIdProductSale => {
            groupedByIdProductSale.foldLeft(0)((acc, productSale2) => acc + productSale2.quantity)
          })
          .map(weekProductSaleTuple => {
            ProductSale(weekProductSaleTuple._1, weekProductSaleTuple._2)
          }).toStream
      ))
      .map(weekResultMap => {
        ShopSale(weekResultMap._1, weekResultMap._2)
      }).toStream
  }

  def computeWeekGlobalSales(allGlobalSales: Stream[GlobalSale]): GlobalSale = {
    GlobalSale(allGlobalSales
      .flatMap(globalSale => globalSale.productSales)
      .groupBy(productSale => productSale.productId)
      .mapValues(groupedByIdProductSale => {
        groupedByIdProductSale.foldLeft(0)((acc, productSale2) => acc + productSale2.quantity)
      })
      .map(weekProductSaleTuple => {
        ProductSale(weekProductSaleTuple._1, weekProductSaleTuple._2)
      }).toStream)
  }

  def computeWeekShopTurnovers(allShopTurnovers: Stream[ShopTurnover]): Stream[ShopTurnover] = {
    allShopTurnovers
      .groupBy(shopSale => shopSale.shopUuid)
      .map(shopTurnoverByUuidMap => (shopTurnoverByUuidMap._1,
        shopTurnoverByUuidMap._2
          .flatMap(shopTurnover => shopTurnover.productTurnovers)
          .groupBy(productTurnover => productTurnover.productId)
          .mapValues(groupedByIdProductTurnover => {
            groupedByIdProductTurnover.foldLeft(0.0)((acc, productTurnover2) => roundValue(acc + productTurnover2.turnover))
          })
          .map(weekProductTurnoverTuple => {
            ProductTurnover(weekProductTurnoverTuple._1, weekProductTurnoverTuple._2)
          }).toStream
      ))
      .map(weekResultMap => {
        ShopTurnover(weekResultMap._1, weekResultMap._2)
      }).toStream
  }

  def computeWeekGlobalTurnovers(allGlobalTurnovers: Stream[GlobalTurnover]): GlobalTurnover = {
    GlobalTurnover(allGlobalTurnovers
      .flatMap(globalTurnover => globalTurnover.productTurnovers)
      .groupBy(productTurnover => productTurnover.productId)
      .mapValues(groupedByIdProductTurnover => {
        groupedByIdProductTurnover.foldLeft(0.0)((acc, productTurnover2) => roundValue(acc + productTurnover2.turnover))
      })
      .map(weekProductSaleTuple => {
        ProductTurnover(weekProductSaleTuple._1, weekProductSaleTuple._2)
      }).toStream)
  }


}
