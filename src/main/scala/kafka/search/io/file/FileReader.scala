package kafka.search.io.file

import kafka.search.io.Reader
import org.mapdb.{DB, DBMaker}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by srastogi on 02-Mar-17.
  */
class FileReader(configurations: Map[String, String]) extends File(configurations) with Reader{
  override val logger: Logger = LoggerFactory.getLogger(this.getClass)

  // Create File database
  override lazy val fileDb: DB = DBMaker.fileDB(filePath)
    .fileMmapEnableIfSupported()
    .checksumHeaderBypass()
    .fileLockDisable()
    .readOnly()
    .make()

  override def contains[A](key: A): Boolean = {
    logger.debug("Checking if the following key exists: " + key.toString)
    diskMap.containsKey(key)
  }

  override def get[A](key: A): Option[String] = {
    logger.debug("Getting value for key: " + key.toString)
    diskMap.get(key) match{
      case null => None
      case res: String => Some(res)
    }
  }

  override def close(): Unit = {
    logger.debug("Closing FileReader.")
    fileDb.close()
  }
}
