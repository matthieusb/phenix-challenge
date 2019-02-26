package phenix

import java.nio.file.{Path, Paths}
import java.text.SimpleDateFormat
import java.util.Date

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import phenix.TransactionMarshaller.{marshallFile, marshallLineString}

import scala.util.{Failure, Success, Try}

case class Transaction(transactionId: Int, date: Date, shopUuid: String, productId: Int, quantity: Int)
case class Product(productId: Int, price: Double)

trait FileIngester extends LazyLogging {
  def ingestFile(filePath: Path): Try[Stream[String]] = {
    logger.info(s"Ingestion fichier ${filePath.toAbsolutePath}")
    val fileToIngest = File(filePath.toString)

    if (fileToIngest.exists)
      Success(fileToIngest.lineIterator.toStream)
    else
      Failure(new IllegalArgumentException(s"Fichier ${filePath.toAbsolutePath} introuvable"))
  }
}

trait Marshaller[T] extends FileIngester {
  val CARREFOUR_HORIZONTAL_SEPARATOR = """\|"""
  val CARREFOUR_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ")

  def marshallFile(filePath: Path, deserializeFunction: String => T): Stream[T] = {
    ingestFile(filePath) match { // TODO This should not be done bythe marshaller but by a wrapper object around it
      case Success(lines) => lines.map(line => deserializeFunction(line))
      case Failure(throwable) =>
        logger.error(throwable.getMessage)
        Stream.empty
    }
  }

  def marshallLines(filePath: Path): Stream[T] = {
    marshallFile(filePath, marshallLineString)
  }

  // NOTE: For now this method should not consider any possible errors in the source data
  // But It should be done in the future
  def marshallLineString(line: String): T
}

object TransactionMarshaller extends Marshaller[Transaction] with LazyLogging {
  override def marshallLineString(line: String): Transaction = {
    line.split(CARREFOUR_HORIZONTAL_SEPARATOR) match {
      case Array(transactionId, dateAsString, shopUuid, productId, quantity) =>
        Transaction(transactionId.toInt, CARREFOUR_DATE_FORMAT.parse(dateAsString), shopUuid, productId.toInt, quantity.toInt)
    }
  }
}

object ProductMarshaller extends Marshaller[Product] with LazyLogging {
  override def marshallLineString(line: String): Product = {
    line.split(CARREFOUR_HORIZONTAL_SEPARATOR) match {
      case Array(productId, price) => Product(productId.toInt, price.toDouble)
    }
  }
}


object Main extends App with LazyLogging {
  logger.info("Début du programme")
  // TODO Do the necessary calls and CLI handling
}

