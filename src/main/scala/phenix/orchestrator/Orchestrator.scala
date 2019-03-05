package phenix.orchestrator

import com.typesafe.scalalogging.LazyLogging
import phenix.calculator.{DayKpiCalculator, WeekKpiCalculator}
import phenix.model._


/**
  * Orchestrates all the operations for this project.
  *
  * Makes the calls to several other orchestrators.
  */
object Orchestrator extends LazyLogging {

  def launchProcess(arguments: FolderArguments): Unit = {
    logger.info("Launching all calculations")
    logger.info(s"Input folder is ${arguments.inputFolder} | Output folder is ${arguments.outputFolder}")

    val inputFiles = FileOrchestrator.determineInputFiles(arguments)

    (inputFiles.inputTransactionsFiles.isEmpty, inputFiles.inputProductFiles.isEmpty) match {
      case (true, true) => logger.error("No transactions or products data files found")
      case (true, _) => logger.error("No transactions data files found")
      case (_, true) => logger.error("No products files found")
      case (false, false) => {
        val allCompleteDayKpi = doCalculationsByDay(inputFiles)

        allCompleteDayKpi.foreach(sortedCompleteDayKpi => {
          FileOrchestrator.outputCompleteDayKpi(arguments.outputFolder, sortedCompleteDayKpi.truncateTop100())
        })

        if (!arguments.simpleCalc) { // Week calculations done only if no -s flag in cli
          val weekKpi = doWeekCalculations(allCompleteDayKpi)
          FileOrchestrator.outputWeekKpi(arguments.outputFolder, weekKpi.truncateTop100())
        }
      }
    }
  }

  def doCalculationsByDay(inputFiles: InputFiles): Stream[CompleteDayKpi] = {
    val marshalledGroupedRecords = groupSortMarshalledInputValuesByDate(FileOrchestrator.convertInputFilesToMarshalledValues(inputFiles))

    marshalledGroupedRecords.keys.map(transactionsKey => {
      DayKpiCalculator.computeDayKpi(transactionsKey, marshalledGroupedRecords(transactionsKey)).sortResults()
    }).toStream
  }

  def doWeekCalculations(allCompleteDayKpi: Stream[CompleteDayKpi]): WeekKpi = {
    val lastDayDate = allCompleteDayKpi.take(1) match {
      case element #:: Stream.Empty => element.date
    }


    // TODO Check if the right files are being gotten for week calculation
    // Example: case where last 7 dates are further than a week old

    WeekKpiCalculator.computeWeekKpi(lastDayDate, allCompleteDayKpi.take(7)) // TODO Check if these are sorted by date, this is important
  }



  /**
    * Sorts the Transactions and products extract from a file so that they are in descending order.
    *
    * @param marshalledInputRecords
    * @return
    */
  def groupSortMarshalledInputValuesByDate(marshalledInputRecords: (Stream[Transactions], Stream[Products])): Map[Transactions, Stream[Products]] = {
    val transactionsRecords = marshalledInputRecords._1
    val productsRecords = marshalledInputRecords._2

    transactionsRecords.sorted.reverse
      .map(transactionsRecord => {
        (transactionsRecord, productsRecords.filter(products => products.metaData.date == transactionsRecord.metaData.date))
      }).toMap
  }
}



