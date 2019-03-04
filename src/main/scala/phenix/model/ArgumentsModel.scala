package phenix.model

import java.nio.file.{Path, Paths}

import org.rogach.scallop.{ScallopConf, ScallopOption}

class ArgumentsConfig(arguments: Seq[String]) extends ScallopConf(arguments) {
  val inputFolder: ScallopOption[String] = opt[String](
    descr = "The folder which contains the data files. FAILS if it does not exist",
    required = true,
    validate = { checkFolderExistence }
  )

  val outputFolder: ScallopOption[String] = opt[String](default = Some("phenix-output"), descr = "The folder where you want the results files to go. If not mentioned, will put the results in ./phenix-output")

  def checkFolderExistence(path: String): Boolean = {
    Paths.get(path).toAbsolutePath
      .toFile
      .isDirectory
  }

  verify()
}

case class FolderArguments(inputFolder: Path, outputFolder: Path)