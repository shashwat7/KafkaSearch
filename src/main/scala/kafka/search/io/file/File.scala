package kafka.search.io.file

import kafka.search.io.{Reader, Writer}
import org.mapdb.{DB, DBMaker, HTreeMap, Serializer}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by srastogi on 01-Mar-17.
  */
class File(configurations: Map[String, String]){
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val filePath: String = configurations("file.path")
  val maxFileSize = configurations.getOrElse("file.max.size", (16*1024*1024*1024).toString).toLong // Default: 16GB

  // Create File database
  lazy val fileDb: DB = DBMaker.fileDB(filePath)
    .fileMmapEnableIfSupported()
    .checksumHeaderBypass()
    .fileLockDisable()
    .make()
  lazy val diskMap = createDiskMap[String, String]

  // TODO: Make a generic map based on [A,B]
  def createDiskMap[A,B]: HTreeMap[String,String] = {
    val keySerializer = Serializer.STRING
    val valueSerializer = Serializer.STRING
    fileDb.hashMap("kafka_search", keySerializer, valueSerializer)
      .expireStoreSize(maxFileSize)
      .expireAfterCreate()
      .createOrOpen()
  }

}
