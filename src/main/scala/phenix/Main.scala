package phenix

import java.nio.file.Paths

import phenix.model.{ArgumentsConfig, FolderArguments}
import phenix.orchestrator.Orchestrator

object Main extends App {
  val argumentsConf = new ArgumentsConfig(args)

  val folderArguments = FolderArguments(Paths.get(argumentsConf.inputFolder()), Paths.get(argumentsConf.outputFolder()), argumentsConf.simpleCalc())
  Orchestrator.launchProcess(folderArguments)
}

