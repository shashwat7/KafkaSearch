package kafka.search.io.cassandra

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.util.Unsafe

/**
  * Created by srastogi on 14-Mar-17.
  */
class Cassandra(configurations: Map[String, String]) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

}
