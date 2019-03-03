package phenix.model

import java.nio.file.Path

trait FileOutputName {
  val outputName: String
}

case class FileOutput(outputName: String, fileContentOutput: Stream[String]) extends FileOutputName

case class KpiOutput(outputPath: Path, fileOutputs: Stream[FileOutput])