package phenix

import com.typesafe.scalalogging.LazyLogging


object Main extends App with LazyLogging {
  logger.info("-- Début du programme")
  Orchestrator.launchCalculations()
}

