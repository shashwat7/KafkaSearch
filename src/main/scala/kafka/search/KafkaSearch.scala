package kafka.search

import kafka.search.common.Utils
import kafka.search.io.Reader
import kafka.search.io.file.FileReader
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by srastogi on 02-Mar-17.
  */
object KafkaSearch {


  private val usage: String =
    """
      | KafkaSearch will search for the key in the kafka db
      | Usage: java -cp KafkaSearch-assembly-1.0.jar KafkaSearch [ConfigFile] [GetValue=0/ContainsKey=1] [Key(s)-separated by space]
    """.stripMargin

  val defaultConfigFile = "conf/consumer.properties"
  val logger: Logger = LoggerFactory.getLogger(KafkaSearch.getClass)

  def main(args: Array[String]): Unit = {

    if (args.length < 3) {
      println(usage)
      System.exit(1)
    }

    // Read configurations
    val configFilePath = args(0)
    val configurations = Utils.configReader(configFilePath)

    // Create a reader
    val reader = createReader(
      source = configurations.get("search.from").map(_.toLowerCase),
      config = configurations
    )

    // Start Search
    search(reader,
      operation = args(1),
      keys = args.slice(2, args.length),
      outputFilePath = configurations.getOrElse("results.path", "kafka_search.output")
    )

    // Shutdown reader when application is shutdown
    Runtime.getRuntime.addShutdownHook(new Thread(){
      override def run(): Unit = {
        reader.close() // Shutdown reader
      }
    })

  }

  def createReader(source: Option[String],
                   config: Map[String, String]): Reader = {
    source match {
      case Some("file") =>
        logger.info("Reading data from Files.")
        getFileReader(config)
      case Some(_) | None =>
        logger.warn("No appropriate values found for property 'search.from'. Setting default(FILE).")
        getFileReader(config)
    }
  }

  def search(reader: kafka.search.io.Reader,
             operation: String,
             keys: Array[String],
             outputFilePath: String): Unit = operation match{
    case "0" => printResults(outputFilePath, keys, reader, getValues)
    case "1" => printResults(outputFilePath, keys, reader, containKeys)
    case _ => logger.error(
      """
        |Second parameter must be a numeric value.
        | 0 => To get the value corresponding the keys
        | 1 => To find if the keys exists or not
      """.stripMargin)
      System.exit(2)
  }

  def getFileReader(configurations: Map[String, String]): kafka.search.io.Reader = new FileReader(configurations)

  def getValues(keys: Array[String], reader: kafka.search.io.Reader): Array[String] = {
    keys.map{ key =>
      reader.get(key) match{
        case None => "NO_RESULT_FOUND"
        case Some(res) => res
      }
    }
  }

  def containKeys(keys: Array[String], reader: kafka.search.io.Reader): Array[Boolean] = {
    keys.map{ key => reader.contains(key)}
  }

  def printResults[A](file: String,
                      keys: Array[String],
                      reader: kafka.search.io.Reader,
                      f: (Array[String], kafka.search.io.Reader) => Array[A]) = {
    Utils.printAndLogToAFile(file, "\nRESULTS:\n")
    (keys zip f(keys, reader)).foreach{case (key, value) => Utils.printAndLogToAFile(file, key + " => " + value)}
    Utils.printAndLogToAFile(file, "---------------------------------------------------")
  }

}
