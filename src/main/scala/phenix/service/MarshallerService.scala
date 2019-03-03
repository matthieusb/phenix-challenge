package phenix.service

import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import phenix.model._

import scala.util.{Failure, Success, Try}

trait Marshaller[T, U, V] extends FileIngester {
  val CARREFOUR_HORIZONTAL_SEPARATOR: String = """\|"""
  val CARREFOUR_FILENAME_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
  val CARREFOUR_FILE_METADATA_SEPARATOR: String = "_"

  def marshallFile(filePath: Path, deserializeFunction: String => U): Try[Stream[U]] = {
    ingestRecordFile(filePath) match {
      case Success(lines) => Success(lines.map(line => deserializeFunction(line)))
      case Failure(throwable) => Failure(throwable)
    }
  }

  def marshallLines(filePath: Path): Try[T]

  // NOTE: For now this method should not consider any possible errors in the source data
  def marshallLineString(line: String): U

  // NOTE: For now this method should not consider any possible errors in the source filename
  def marshallFileName(filePath: Path): Try[V]
}

object TransactionMarshaller extends Marshaller[Transactions, Transaction, TransactionFileMetaData] {
  override def marshallLineString(line: String): Transaction = {
    line.split(CARREFOUR_HORIZONTAL_SEPARATOR) match {
      case Array(transactionId, _, shopUuid, productId, quantity) =>
        Transaction(transactionId.toInt, shopUuid, productId.toInt, quantity.toInt)
    }
  }

  override def marshallLines(filePath: Path): Try[Transactions] = {
    (marshallFile(filePath, marshallLineString), marshallFileName(filePath)) match {
      case (Success(transactionStream), Success(transactionMetaData)) => Success(Transactions(transactionStream, transactionMetaData))
      case (Failure(throwable), Success(_)) => Failure(new IllegalArgumentException(throwable.getMessage))
      case (Success(_), Failure(throwable)) => Failure(new IllegalArgumentException(throwable.getMessage))
      case (Failure(throwableStream), Failure(throwableMetadata)) => Failure(new IllegalArgumentException(s"Erreurs multiples: $throwableStream ; $throwableMetadata"))
    }
  }

  override def marshallFileName(filePath: Path): Try[TransactionFileMetaData] = {
    val transactionFileName = filePath.getFileName.toString
      .replaceFirst(CARREFOUR_DATA_FILE_EXTENSION, "")
    transactionFileName.split(CARREFOUR_FILE_METADATA_SEPARATOR) match {
      case Array(_, dateStr) => Success(TransactionFileMetaData(LocalDate.parse(dateStr, CARREFOUR_FILENAME_DATE_FORMAT)))
      case _ => Failure(new IllegalArgumentException(s"Nom du fichier de transaction invalides: $transactionFileName"))
    }
  }
}

object ProductMarshaller extends Marshaller[Products, Product, ProductFileMetaData] {
  val CARREFOUR_PRODUCT_FILE_PREFIX = "reference_prod-"

  override def marshallLineString(line: String): Product = {
    line.split(CARREFOUR_HORIZONTAL_SEPARATOR) match {
      case Array(productId, price) => Product(productId.toInt, price.toDouble)
    }
  }

  override def marshallLines(filePath: Path): Try[Products] = {
    (marshallFile(filePath, marshallLineString), marshallFileName(filePath)) match {
      case (Success(productStream), Success(productsMetaData)) => Success(Products(productStream, productsMetaData))
      case (Failure(throwable), Success(_)) => Failure(new IllegalArgumentException(throwable.getMessage))
      case (Success(_), Failure(throwable)) => Failure(new IllegalArgumentException(throwable.getMessage))
      case (Failure(throwableStream), Failure(throwableMetadata)) => Failure(new IllegalArgumentException(s"Erreurs multiples: $throwableStream ; $throwableMetadata"))
    }
  }

  override def marshallFileName(filePath: Path): Try[ProductFileMetaData] = {
    val productFileName = filePath.getFileName.toString
    productFileName
      .replaceFirst(CARREFOUR_PRODUCT_FILE_PREFIX, "")
      .replaceFirst(CARREFOUR_DATA_FILE_EXTENSION, "")
      .split(CARREFOUR_FILE_METADATA_SEPARATOR) match {
      case Array(shopUuid, dateStr) => Success(ProductFileMetaData(shopUuid, LocalDate.parse(dateStr, CARREFOUR_FILENAME_DATE_FORMAT)))
      case _ => Failure(new IllegalArgumentException(s"Nom du fichier de produits invalide $productFileName"))
    }
  }
}
