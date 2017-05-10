package kafka.search.common

import java.io.FileWriter
import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source

/**
  * Created by srastogi on 28-Feb-17.
  */
object Utils {

  val logger: Logger = LoggerFactory.getLogger(Utils.getClass)

  def configReader(path: String): Map[String,String] = {
    logger.debug("Reading configuration file: " + path)
    val src = Source.fromFile(path)
    val propertyMap = src.getLines().filterNot(line => line.startsWith("#") || line.trim().equals(""))
      .map(line => {
        val pair = line.split("=")
        pair(0).trim() -> pair(1).trim()
      }).toMap
    propertyMap.foreach{case (key, value) => logger.debug(key + " -> " + value)}
    propertyMap
  }

  def logToAFile(f: String, str: String) = {
    val fw = new FileWriter(f, true)
    fw.write(str + "\n")
    fw.close()
  }

  def printAndLogToAFile(f: String, str: String) = {
    logToAFile(f, str)
    println(str)
  }

}
