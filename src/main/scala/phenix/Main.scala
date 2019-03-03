package phenix

import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import phenix.model.{ArgumentsConfig, FolderArguments}



object Main extends App with LazyLogging {
  val argumentsConf = new ArgumentsConfig(args)
  Orchestrator.launchProcess(FolderArguments(Paths.get(argumentsConf.inputFolder()), Paths.get(argumentsConf.outputFolder())))
}

