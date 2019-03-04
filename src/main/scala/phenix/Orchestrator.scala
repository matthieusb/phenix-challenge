package phenix

import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import phenix.calculator.IndicatorCalculator
import phenix.model.{FolderArguments, CompleteDayKpi, FileOutput, KpiOutput}
import phenix.service._
import scalaz.std.stream.streamSyntax._

/**
  * Orchestrates all the operations for this project.
  *
  * Makes the calls to several other orchestrators.
  */
object Orchestrator extends LazyLogging {

  def launchProcess(arguments: FolderArguments): Unit = {
    logger.info("Launching all calculations")
    logger.info(s"Input folder is ${arguments.inputFolder} | Output folder is ${arguments.outputFolder}")

    lazy val productFileNames = List(
      "data/input/simple-example/reference_prod-shopuuid1_20170514.data",
      "data/input/simple-example/reference_prod-shopuuid2_20170514.data"
    ).toStream

    lazy val transactionFileName = "data/input/simple-example/transactions_20170514.data"

//        lazy val productFileNames = List(
//        "data/input/example/reference_prod-2a4b6b81-5aa2-4ad8-8ba9-ae1a006e7d71_20170514.data",
//        "data/input/example/reference_prod-6af0502b-ce7a-4a6f-b5d3-516d09514095_20170514.data",
//        "data/input/example/reference_prod-8e588f2f-d19e-436c-952f-1cdd9f0b12b0_20170514.data",
//        "data/input/example/reference_prod-10f2f3e6-f728-41f3-b079-43b0aa758292_20170514.data",
//        "data/input/example/reference_prod-72a2876c-bc8b-4f35-8882-8d661fac2606_20170514.data",
//        "data/input/example/reference_prod-29366c83-eae9-42d3-a8af-f15339830dc5_20170514.data",
//        "data/input/example/reference_prod-af068240-8198-4b79-9cf9-c28c0db65f63_20170514.data",
//        "data/input/example/reference_prod-bdc2a431-797d-4b07-9567-67c565a67b84_20170514.data",
//        "data/input/example/reference_prod-bf0999da-ae45-49df-983e-89020198330b_20170514.data",
//        "data/input/example/reference_prod-d4bfbabf-0160-4e87-8688-78e0943a396a_20170514.data",
//        "data/input/example/reference_prod-dd43720c-be43-41b6-bc4a-ac4beabd0d9b_20170514.data",
//        "data/input/example/reference_prod-e3d54d00-18be-45e1-b648-41147638bafe_20170514.data"
//        ).toStream
//        lazy val transactionFileName = "data/input/example/transactions_20170514.data"

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
}

/**
  * Orchectrates operations on files for this project.
  *
  * Has the following responsibilities:
  * - getting the right files out of a directory.
  * - outputting the results given correctly in a directory.
  */
object FileOrchestrator extends LazyLogging with FileProducer {
  def outputCompleteDayKpi(arguments: FolderArguments, completeDayKpi: CompleteDayKpi): Unit = {
    val dayShopSalesFileOutput = completeDayKpi.dayShopSales.map(dayShopSale => {
      FileOutput(ProductSaleFileNameService.generateDayByShopFileName(completeDayKpi.date, dayShopSale.shopUuid),
        ProductSaleUnmarshaller.unmarshallRecords(completeDayKpi.dayGlobalSales.productSales))
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

    // TODO Define output path with parameter rather than manually
    outputKpis(KpiOutput(arguments.outputFolder, completeFileOutputs))
  }

  def outputKpis(kpiOutput: KpiOutput): Unit = {
    kpiOutput.fileOutputs.foreach(fileOutput => {
      writeRecordFile(
        kpiOutput.outputPath.resolve(fileOutput.outputName),
      fileOutput.fileContentOutput)
    })
  }
}


