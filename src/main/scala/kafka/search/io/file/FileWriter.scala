package kafka.search.io.file

import kafka.search.io.Writer
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by srastogi on 02-Mar-17.
  */
class FileWriter(configurations: Map[String, String]) extends File(configurations) with Writer{
  override val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def write_key_value[A, B](key: A, value: B) = {
    logger.debug("Writing (key, value) : " + key.toString + "," + value.toString)
    diskMap.put(key.toString, value.toString)
    true // TODO: Check return value
  }

  override def close(): Unit = {
    logger.debug("Closing FileWriter.")
    fileDb.close()
  }

}
