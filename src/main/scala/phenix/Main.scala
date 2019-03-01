package phenix

import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import phenix.calculator.IndicatorCalculator
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

    val transactions = TransactionMarshaller.marshallLines(Paths.get(transactionFileName))
    transactions.map(transactions => {
      val productsStream = productFileNames.map(productFileName => {
        ProductMarshaller.marshallLines(Paths.get(productFileName)).get
      })

      // TODO Put in a variable and write to a file
      println("---------- RESULTAT")
      println(IndicatorCalculator.computeDayKpi(transactions, productsStream))
    })
  }
}

object Main extends App with LazyLogging {
  logger.info("-- Début du programme")
  Orchestrator.launchCalculations()
  // TODO Do the necessary calls and CLI handling
}

