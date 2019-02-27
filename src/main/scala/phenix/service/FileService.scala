package phenix.service

import java.nio.file.Path

import better.files.File
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

trait fileProducer extends LazyLogging {

}

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

