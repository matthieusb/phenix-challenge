package phenix.orchestrator

import java.time.LocalDate

import com.typesafe.scalalogging.LazyLogging
import phenix.calculator.{DayKpiCalculator, WeekKpiCalculator}
import phenix.model._


/**
  * Orchestrates all the operations for this project.
  *
  * Makes the calls to several other orchestrators.
  */
object Orchestrator extends LazyLogging {
  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.by(_.toEpochDay)

  /**
    * Calls the different calculations according to input arguments.
    * Outputs errrors if files can't be found in the given input folder.
    *
    * @param arguments the cli passed valid arguments
    */
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
          val completeWeekKpi = doWeekCalculations(allCompleteDayKpi)
          FileOrchestrator.outputWeekKpi(arguments.outputFolder, completeWeekKpi.truncateTop100())
        }
      }
    }
  }

  def doCalculationsByDay(inputFiles: InputFiles): Stream[CompleteDayKpi] = {
    val marshalledGroupedRecords = groupMarshalledInputValuesByDate(FileOrchestrator.convertInputFilesToMarshalledValues(inputFiles))

    marshalledGroupedRecords.keys.map(transactionsKey => {
      DayKpiCalculator.computeDayKpi(transactionsKey, marshalledGroupedRecords(transactionsKey)).sortResults()
    }).toStream
  }

  def doWeekCalculations(allCompleteDayKpi: Stream[CompleteDayKpi]): CompleteWeekKpi = {
    val lastDayDate = allCompleteDayKpi.maxBy(completeDayKpi => completeDayKpi.date).date

    val sevenDaysBeforeLastDayDate = lastDayDate.minusDays(7)
    val completeDayKpiWithin7Days = allCompleteDayKpi
      .filter(completeDayKpi => completeDayKpi.date.isAfter(sevenDaysBeforeLastDayDate) ||
        completeDayKpi.date.equals(sevenDaysBeforeLastDayDate))

    WeekKpiCalculator.computeWeekKpi(lastDayDate, completeDayKpiWithin7Days.take(7))
  }

  /**
    * Associates the Transactions and to the right products extracted files.
    * They are associated by date.
    *
    * @param marshalledInputRecords the purely marshalled files, without associations between them
    * @return
    */
  def groupMarshalledInputValuesByDate(marshalledInputRecords: (Stream[Transactions], Stream[Products])): Map[Transactions, Stream[Products]] = {
    val transactionsRecords = marshalledInputRecords._1
    val productsRecords = marshalledInputRecords._2

    transactionsRecords
      .map(transactionsRecord => {
        (transactionsRecord, productsRecords.filter(products => products.metaData.date.equals(transactionsRecord.metaData.date)))
      }).toMap
  }
}



