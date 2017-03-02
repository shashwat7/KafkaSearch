package kafka.search

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import kafka.search.common.Utils
import kafka.search.consumer.ConsumerLoop
import kafka.search.io.file.FileWriter
import kafka.search.io.Writer
import org.slf4j.{Logger, LoggerFactory}

import scala.language.postfixOps

/**
  * Created by srastogi on 28-Feb-17.
  */
object StartConsuming {

  private val usage: String =
    """
      | StartConsuming will start consuming message from a particular kafka topic.
      | Usage: java -cp KafkaSearch-assembly-1.0.jar StartConsuming [ConfigFile]
    """.stripMargin

  val defaultConfigFile = "conf/consumer.properties"
  val logger: Logger = LoggerFactory.getLogger(StartConsuming.getClass)

  def main(args: Array[String]): Unit = {

    if(args.length != 1) {
      println(usage)
      System.exit(1)
    }

    // Read configurations
    val configFilePath = args(0)
    val configurations = Utils.configReader(configFilePath)
    val topics: List[String] = configurations("kafka.topic").split(",").map(_.trim).toList
    val numConsumers: Int = configurations.getOrElse("number.of.consumers", "1").toInt
    val writeTo: Option[String] = configurations.get("write.to").map(_.toLowerCase)

    // Create a writer
    val writer: Writer = writeTo match {
      case Some("file") =>
        logger.info("Writing data to Files.")
        getFileWriter(configurations)
      case Some(_) | None =>
        logger.warn("No appropriate values found for property 'write.to'. Setting default(FILE).")
        getFileWriter(configurations)
    }

    // Start a ExecutorService
    val executor: ExecutorService = Executors.newFixedThreadPool(numConsumers)
    val consumers: List[ConsumerLoop] = 0 until numConsumers map{ id =>
      val consumer = new ConsumerLoop(id, topics, configurations, writer)
      executor.submit(consumer)
      consumer
    } toList

    // Shutdown all consumers when application is shutdown
    Runtime.getRuntime.addShutdownHook(new Thread(){
      override def run(): Unit = {
        consumers.foreach{ consumer => consumer.shutdown() } // Shutdown all consumers
        writer.close() // Shutdown writer
        executor.shutdown()
        try{
          executor.awaitTermination(5000, TimeUnit.MILLISECONDS)
        } catch{
          case ie: InterruptedException => ie.printStackTrace()
        }
      }
    })

  }

  def getFileWriter(configurations: Map[String, String]): FileWriter = new FileWriter(configurations)

}
