package phenix

import phenix.model.{ArgumentsConfig, FolderArguments}
import phenix.orchestrator.Orchestrator

object Main extends App {
  val argumentsConf = new ArgumentsConfig(args)
  val folderArguments = new FolderArguments(argumentsConf)
  Orchestrator.launchProcess(folderArguments)
}

