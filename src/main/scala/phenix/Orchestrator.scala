package phenix

import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import phenix.calculator.IndicatorCalculator
import phenix.model._
import phenix.service._
import scalaz.std.stream.streamSyntax._

/**
  * Orchestrates operations on files for this project.
  *
  * Has the following responsibilities:
  * - getting the right files out of a directory.
  * - outputting the results given correctly in a directory.
  */
object FileOrchestrator extends LazyLogging with FileIngester with FileProducer with FileNameChecker {

  def determineInputFiles(arguments: FolderArguments): InputFiles  = {
    val inputFilesList = arguments
      .inputFolder.toFile.listFiles().toStream

    val inputTransactionsList = inputFilesList
      .filter(inputFile => fileIsTransactionRecord(inputFile.getName))

    val inputProductsList = inputFilesList
      .filter(inputFile => fileIsProductRecord(inputFile.getName))

    InputFiles(inputTransactionsList, inputProductsList)
  }

  def convertInputFilesToMarshalledValues(inputFiles: InputFiles): (Stream[Transactions], Stream[Products]) = {
    val transactions = inputFiles.inputTransactionsFiles.map(inputTransactionFile => {
      TransactionMarshaller.marshallLines(ingestRecordFile(inputTransactionFile.toPath.toAbsolutePath), inputTransactionFile.getName)
    })

    val products = inputFiles.inputProductFiles.map(inputProductFile => {
      ProductMarshaller.marshallLines(ingestRecordFile(inputProductFile.toPath.toAbsolutePath), inputProductFile.getName)
    })

    (transactions, products)
  }

  def outputCompleteDayKpi(outputFolder: Path, completeDayKpi: CompleteDayKpi): Unit = {
    val dayShopSalesFileOutput = completeDayKpi.dayShopSales.map(dayShopSale => {
      FileOutput(ProductSaleFileNameService.generateDayByShopFileName(completeDayKpi.date, dayShopSale.shopUuid),
        ProductSaleUnmarshaller.unmarshallRecords(dayShopSale.productSales))
    })

    val dayGlobalSale = Stream(FileOutput(ProductSaleFileNameService.generateDayGlobalFileName(completeDayKpi.date),
      ProductSaleUnmarshaller.unmarshallRecords(completeDayKpi.dayGlobalSales.productSales)))

    val dayShopTurnoversFileOutput = completeDayKpi.dayShopTurnovers.map(dayShopTurnover => {
      FileOutput(ProductTurnoverFileNameService.generateDayByShopFileName(completeDayKpi.date, dayShopTurnover.shopUuid),
        ProductTurnoverUnmarshaller.unmarshallRecords(dayShopTurnover.productTurnovers))
    })

    val dayGlobalTurnover = Stream(FileOutput(ProductTurnoverFileNameService.generateDayGlobalFileName(completeDayKpi.date),
      ProductTurnoverUnmarshaller.unmarshallRecords(completeDayKpi.dayGlobalTurnover.productTurnovers)))

    val completeFileOutputs = dayShopSalesFileOutput
      .interleave(dayGlobalSale)
      .interleave(dayShopTurnoversFileOutput)
      .interleave(dayGlobalTurnover)

    outputKpis(KpiOutput(outputFolder, completeFileOutputs))
  }

  def outputKpis(kpiOutput: KpiOutput): Unit = {
    kpiOutput.fileOutputs.foreach(fileOutput => {
      writeRecordFile(
        kpiOutput.outputPath.resolve(fileOutput.outputName),
        fileOutput.fileContentOutput)
    })
  }
}


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
        val marshalledGroupedRecords = groupSortMarshalledInputValuesByDate(FileOrchestrator.convertInputFilesToMarshalledValues(inputFiles))

         val allCompleteDayKpi = marshalledGroupedRecords.keys.map(transactionsKey => {
          CompleteDayKpi.sortDayKpiResults(IndicatorCalculator.computeDayKpi(transactionsKey, marshalledGroupedRecords(transactionsKey)))
        }).toStream

        allCompleteDayKpi.foreach(sortedCompleteDayKpi => {
          FileOrchestrator.outputCompleteDayKpi(arguments.outputFolder, sortedCompleteDayKpi)
        })

        // TODO truncate the results to top 100
        // TODO Add calcultions for 7 days
      }
    }

    //    val transactions = TransactionMarshaller.marshallLines(Paths.get(transactionFileName))
    //    transactions.map(transactions => {
    //      val productsStream = productFileNames.map(productFileName => {
    //        ProductMarshaller.marshallLines(Paths.get(productFileName)).get
    //      })
    //
    //      val sortedDayKpiResults = CompleteDayKpi.sortDayKpiResults(IndicatorCalculator.computeDayKpi(transactions, productsStream))
    //      FileOrchestrator.outputCompleteDayKpi(arguments, sortedDayKpiResults) // TODO Add truncated results
    //    })
  }

  def groupSortMarshalledInputValuesByDate(marshalledInputRecords: (Stream[Transactions], Stream[Products])): Map[Transactions, Stream[Products]] = {
    val transactionsRecords = marshalledInputRecords._1
    val productsRecords = marshalledInputRecords._2

    transactionsRecords.sorted.reverse
      .map(transactionsRecord => {
      (transactionsRecord, productsRecords.filter(products => products.metaData.date == transactionsRecord.metaData.date))
    }).toMap
  }
}



