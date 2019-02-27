package phenix.service

import java.nio.file.Path
import java.text.SimpleDateFormat

import phenix.model._

import scala.util.{Failure, Success, Try}


trait Marshaller[T, U, V] extends FileIngester {
  val CARREFOUR_HORIZONTAL_SEPARATOR = """\|"""
  val CARREFOUR_FILENAME_DATE_FORMAT = new SimpleDateFormat("YYYYMMDD")
  val CARREFOUR_FILE_METADATA_SEPARATOR = "_"

  // TODO Refactor: the file ingestion should be done prior to the marhsaller by an orchestrator object
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
  val CARREFOUR_TRANSACTION_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ")

  override def marshallLineString(line: String): Transaction = {
    line.split(CARREFOUR_HORIZONTAL_SEPARATOR) match {
      case Array(transactionId, dateAsString, shopUuid, productId, quantity) =>
        Transaction(transactionId.toInt, CARREFOUR_TRANSACTION_DATE_FORMAT.parse(dateAsString), shopUuid, productId.toInt, quantity.toInt)
    }
  }

  // TODO This should be mutualized
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
    transactionFileName.split(CARREFOUR_FILE_METADATA_SEPARATOR) match {
      case Array(_, dateAsString) => Success(TransactionFileMetaData(CARREFOUR_FILENAME_DATE_FORMAT.parse(dateAsString)))
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

  // TODO This should be mutualized
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
      .split(CARREFOUR_FILE_METADATA_SEPARATOR) match {
      case Array(shopUuid, dateAsString) => Success(ProductFileMetaData(shopUuid, CARREFOUR_FILENAME_DATE_FORMAT.parse(dateAsString)))
      case _ => Failure(new IllegalArgumentException(s"Nom du fichier de produits invalide $productFileName"))
    }
  }
}
