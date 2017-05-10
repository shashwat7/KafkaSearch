package kafka.search.io.file

import org.mapdb.{DB, DBMaker, HTreeMap, Serializer}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by srastogi on 01-Mar-17.
  */
//case class DbDoesNotExistsException(error: String) extends Exception

class File(configurations: Map[String, String]){
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val filePath: String = configurations("file.path")
  val maxFileSize = configurations.getOrElse("file.max.size", (16*1024*1024*1024).toString).toLong // Default: 16GB

  lazy val fileDb: DB = createFileDB

  // Create File database
  def createFileDB: DB = {
    logger.debug("Creating a writable DB instance.")
    DBMaker.fileDB(filePath)
      .fileMmapEnableIfSupported()
      .checksumHeaderBypass()
      .fileLockDisable()
      .make()
  }

  // TODO: Make a generic map based on [A,B]
  def createDiskMap[A,B]: HTreeMap[String,String] = {
    val keySerializer = Serializer.STRING
    val valueSerializer = Serializer.STRING
    fileDb.hashMap("kafka_search", keySerializer, valueSerializer)
      .expireStoreSize(maxFileSize)
      .expireAfterCreate()
      .createOrOpen()
  }

  def getDiskMap[A,B]: HTreeMap[String,String] = {
    val map = createDiskMap
    logger.debug("Total number of keys already existing map: " + map.keySet().size())
    map
  }

}
