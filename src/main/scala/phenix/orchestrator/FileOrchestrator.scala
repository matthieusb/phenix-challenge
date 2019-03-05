package phenix.orchestrator

import java.nio.file.Path
import java.time.LocalDate

import com.typesafe.scalalogging.LazyLogging
import phenix.model._
import phenix.service._
import scalaz.std.stream.streamSyntax._

/**
  * Handles generating output files with their name and content
  *
  * @tparam T should be used as the per Shop type
  * @tparam U should be used as the per Global type
  */
trait FileOutputOrchestrator[T, U] {
  def generateShopOutput(date: LocalDate, shopdRecords: Stream[T], fileNameFunc: (LocalDate, String) => String): Stream[FileOutput]

  def generatGlobalOutput(date: LocalDate, globalRecord: U, fileNameFunc: LocalDate => String): Stream[FileOutput]
}

object FileOutputProductSaleOrchestrator extends FileOutputOrchestrator[ShopSale, GlobalSale] {
  override def generateShopOutput(date: LocalDate, shopSales: Stream[ShopSale], fileNameFunc: (LocalDate, String) => String): Stream[FileOutput] = {
    shopSales.map(shopSale => {
      FileOutput(fileNameFunc(date, shopSale.shopUuid), ProductSaleUnmarshaller.unmarshallRecords(shopSale.productSales))
    })
  }

  override def generatGlobalOutput(date: LocalDate, globalSale: GlobalSale, fileNameFunc: LocalDate => String): Stream[FileOutput] = {
    Stream(FileOutput(fileNameFunc(date), ProductSaleUnmarshaller.unmarshallRecords(globalSale.productSales)))
  }
}

object FileOutputProductTurnoverOrchestrator extends FileOutputOrchestrator[ShopTurnover, GlobalTurnover] {
  override def generateShopOutput(date: LocalDate, shopTurnovers: Stream[ShopTurnover], fileNameFunc: (LocalDate, String) => String): Stream[FileOutput] = {
    shopTurnovers.map(shopSale => {
      FileOutput(fileNameFunc(date, shopSale.shopUuid), ProductTurnoverUnmarshaller.unmarshallRecords(shopSale.productTurnovers))
    })
  }

  override def generatGlobalOutput(date: LocalDate, globalTurnover: GlobalTurnover, fileNameFunc: LocalDate => String): Stream[FileOutput] = {
    Stream(FileOutput(fileNameFunc(date), ProductTurnoverUnmarshaller.unmarshallRecords(globalTurnover.productTurnovers)))
  }
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
    val dayShopSalesFileOutput = FileOutputProductSaleOrchestrator
      .generateShopOutput(completeDayKpi.date, completeDayKpi.dayShopSales, ProductSaleFileNameService.generateDayShopFileName)
    val dayGlobalSaleOutput = FileOutputProductSaleOrchestrator.generatGlobalOutput(completeDayKpi.date, completeDayKpi.dayGlobalSales, ProductSaleFileNameService.generateDayGlobalFileName)

    val dayShopTurnoversFileOutput =
      FileOutputProductTurnoverOrchestrator.generateShopOutput(completeDayKpi.date, completeDayKpi.dayShopTurnovers, ProductTurnoverFileNameService.generateDayShopFileName)
    val dayGlobalTurnoverOutput = FileOutputProductTurnoverOrchestrator.generatGlobalOutput(completeDayKpi.date, completeDayKpi.dayGlobalTurnover, ProductTurnoverFileNameService.generateDayGlobalFileName)

    mergeAndOutputAllStreams(outputFolder, dayShopSalesFileOutput, dayGlobalSaleOutput, dayShopTurnoversFileOutput, dayGlobalTurnoverOutput)
  }

  def outputWeekKpi(outputFolder: Path, weekKpi: CompleteWeekKpi): Unit = {
    val weekShopSalesFileOutput = FileOutputProductSaleOrchestrator
      .generateShopOutput(weekKpi.lastDayDate, weekKpi.weekShopSales, ProductTurnoverFileNameService.generateWeekShopFileName)
    val weekGlobalSaleOutput = FileOutputProductSaleOrchestrator.generatGlobalOutput(weekKpi.lastDayDate, weekKpi.weekGlobalSales, ProductTurnoverFileNameService.generateWeekGlobalFileName)

    val weekShopTurnoverOutput =
      FileOutputProductTurnoverOrchestrator.generateShopOutput(weekKpi.lastDayDate, weekKpi.weekShopTurnover, ProductTurnoverFileNameService.generateWeekShopFileName)
    val weekGlobalTurnoverOutput = FileOutputProductTurnoverOrchestrator.generatGlobalOutput(weekKpi.lastDayDate, weekKpi.weekGlobalTurnover, ProductTurnoverFileNameService.generateWeekGlobalFileName)

    mergeAndOutputAllStreams(outputFolder, weekShopSalesFileOutput, weekGlobalSaleOutput, weekShopTurnoverOutput, weekGlobalTurnoverOutput)
  }

  def mergeAndOutputAllStreams(outputFolder: Path, fileOutputStreams: Stream[FileOutput]*): Unit = {
    val emptyFileOutputStream : Stream[FileOutput] = Stream()
    val allFileOutputs = fileOutputStreams.foldLeft(emptyFileOutputStream)((acc, stream2) => acc interleave stream2)

    outputKpis(KpiOutput(outputFolder, allFileOutputs))
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
