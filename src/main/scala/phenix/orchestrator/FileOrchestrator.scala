package phenix.orchestrator

import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import phenix.model._
import phenix.service._
import scalaz.std.stream.streamSyntax._


trait FileOutputOrchestrator {
  
}



/**
  * Orchestrates operations on files for this project.
  *
  * Has the following responsibilities:
  * - getting the right files out of a directory.
  * - outputting the results given correctly in a directory.
  */
object FileOrchestrator extends LazyLogging with FileIngester with FileProducer with FileNameChecker {
  val TOP_NUMBER_OF_VALUES = 100

  /**
    * Gets the input files from the input folder mentioned as arguments.
    * Only takes valid transactions and product reference products.
    *
    * See the data folder for examples.
    *
    * @param arguments the cli arguments
    * @return
    */
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

  /**
    * Converts and Writes a CompleteDayKpi to the necessary files.
    * Each stream of results is truncated to the top 100 before being outputted
    *
    * @param outputFolder the folder the files will be written to
    * @param completeDayKpi valid completeDayKpis objects
    */
  def outputCompleteDayKpi(outputFolder: Path, completeDayKpi: CompleteDayKpi): Unit = {
    val dayShopSalesFileOutput = completeDayKpi.dayShopSales.map(dayShopSale => {
      FileOutput(ProductSaleFileNameService.generateDayShopFileName(completeDayKpi.date, dayShopSale.shopUuid),
        ProductSaleUnmarshaller.unmarshallRecords(dayShopSale.productSales))
    })

    val dayGlobalSaleOutput = Stream(FileOutput(ProductSaleFileNameService.generateDayGlobalFileName(completeDayKpi.date),
      ProductSaleUnmarshaller.unmarshallRecords(completeDayKpi.dayGlobalSales.productSales)))

    val dayShopTurnoversFileOutput = completeDayKpi.dayShopTurnovers.map(dayShopTurnover => {
      FileOutput(ProductTurnoverFileNameService.generateDayShopFileName(completeDayKpi.date, dayShopTurnover.shopUuid),
        ProductTurnoverUnmarshaller.unmarshallRecords(dayShopTurnover.productTurnovers))
    })

    val dayGlobalTurnoverOutput = Stream(FileOutput(ProductTurnoverFileNameService.generateDayGlobalFileName(completeDayKpi.date),
      ProductTurnoverUnmarshaller.unmarshallRecords(completeDayKpi.dayGlobalTurnover.productTurnovers)))

    mergeAndOutputAllStreams(outputFolder, dayShopSalesFileOutput, dayGlobalSaleOutput, dayShopTurnoversFileOutput, dayGlobalTurnoverOutput)
  }

  def outputWeekKpi(outputFolder: Path, weekKpi: WeekKpi): Unit = {
    val weekShopSalesFileOutput = weekKpi.weekShopSales.map(weekShopSale => {
      FileOutput(ProductSaleFileNameService.generateWeekShopFileName(weekKpi.lastDayDate, weekShopSale.shopUuid),
        ProductSaleUnmarshaller.unmarshallRecords(weekShopSale.productSales))
    })

    val weekGlobalSaleOutput = Stream(
      FileOutput(ProductSaleFileNameService.generateWeekGlobalFileName(weekKpi.lastDayDate),
        ProductSaleUnmarshaller.unmarshallRecords(weekKpi.weekGlobalSales.productSales))
    )

    val weekShopTurnoverOutput = weekKpi.weekShopTurnover.map(weekShopTurnover => {
      FileOutput(ProductTurnoverFileNameService.generateWeekShopFileName(weekKpi.lastDayDate, weekShopTurnover.shopUuid),
        ProductTurnoverUnmarshaller.unmarshallRecords(weekShopTurnover.productTurnovers))
    })

    val weekGlobalTurnoverOutput = Stream(
      FileOutput(ProductTurnoverFileNameService.generateWeekGlobalFileName(weekKpi.lastDayDate),
        ProductTurnoverUnmarshaller.unmarshallRecords(weekKpi.weekGlobalTurnover.productTurnovers))
    )

    mergeAndOutputAllStreams(outputFolder, weekShopSalesFileOutput, weekGlobalSaleOutput, weekShopTurnoverOutput, weekGlobalTurnoverOutput)
  }

  def mergeAndOutputAllStreams(outputFolder: Path, stream1: Stream[FileOutput], stream2: Stream[FileOutput],
                               stream3: Stream[FileOutput], stream4: Stream[FileOutput]): Unit = {
    val completeFileOutputs = stream1
    .interleave(stream2)
    .interleave(stream3)
    .interleave(stream4)

    outputKpis(KpiOutput(outputFolder, completeFileOutputs))
  }

  /**
    * Write KpiOutput object to a file
    * @param kpiOutput the KpiOutput to write
    */
  def outputKpis(kpiOutput: KpiOutput): Unit = {
    kpiOutput.fileOutputs.foreach(fileOutput => {
      writeRecordFile(
        kpiOutput.outputPath.resolve(fileOutput.outputName),
        fileOutput.fileContentOutput)
    })
  }
}
