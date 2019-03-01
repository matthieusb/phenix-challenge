package phenix

import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import phenix.calculator.IndicatorCalculator
import phenix.model.CompleteDayKpi
import phenix.service.{ProductMarshaller, TransactionMarshaller}

/**
  * Orchestrates operations for this project.
  *
  * Has the responsibility of getting the right files (with the right dates) according to the types of calculations wanted.
  * These files' content should then be fed to IndicatorCalculator.
  */
object Orchestrator extends LazyLogging {

  // TODO This should be replaced with a dynamic version, this is temporary
  def launchCalculations(): Unit = {
    logger.info("Launching all calculations")

    lazy val productFileNames = List(
      "data/input/simple-example/reference_prod-shopuuid1_20170514.data",
      "data/input/simple-example/reference_prod-shopuuid2_20170514.data"
    ).toStream

    lazy val transactionFileName = "data/input/simple-example/transactions_20170514.data"

//    lazy val productFileNames = List(
//    "data/input/example/reference_prod-2a4b6b81-5aa2-4ad8-8ba9-ae1a006e7d71_20170514.data",
//    "data/input/example/reference_prod-6af0502b-ce7a-4a6f-b5d3-516d09514095_20170514.data",
//    "data/input/example/reference_prod-8e588f2f-d19e-436c-952f-1cdd9f0b12b0_20170514.data",
//    "data/input/example/reference_prod-10f2f3e6-f728-41f3-b079-43b0aa758292_20170514.data",
//    "data/input/example/reference_prod-72a2876c-bc8b-4f35-8882-8d661fac2606_20170514.data",
//    "data/input/example/reference_prod-29366c83-eae9-42d3-a8af-f15339830dc5_20170514.data",
//    "data/input/example/reference_prod-af068240-8198-4b79-9cf9-c28c0db65f63_20170514.data",
//    "data/input/example/reference_prod-bdc2a431-797d-4b07-9567-67c565a67b84_20170514.data",
//    "data/input/example/reference_prod-bf0999da-ae45-49df-983e-89020198330b_20170514.data",
//    "data/input/example/reference_prod-d4bfbabf-0160-4e87-8688-78e0943a396a_20170514.data",
//    "data/input/example/reference_prod-dd43720c-be43-41b6-bc4a-ac4beabd0d9b_20170514.data",
//    "data/input/example/reference_prod-e3d54d00-18be-45e1-b648-41147638bafe_20170514.data"
//    ).toStream
//    lazy val transactionFileName = "data/input/example/transactions_20170514.data"

    val transactions = TransactionMarshaller.marshallLines(Paths.get(transactionFileName))
    transactions.map(transactions => {
      val productsStream = productFileNames.map(productFileName => {
        ProductMarshaller.marshallLines(Paths.get(productFileName)).get
      })

      val dayKpiResults = IndicatorCalculator.computeDayKpi(transactions, productsStream)
      outputCompleteDayKpi(dayKpiResults)
      logger.error(dayKpiResults.toString)
    })
  }

  def outputCompleteDayKpi(completeDayKpi: CompleteDayKpi): Unit = {

  }


}

object Main extends App with LazyLogging {
  logger.info("-- Début du programme")
  Orchestrator.launchCalculations()
  // TODO Do the necessary calls and CLI handling
}

