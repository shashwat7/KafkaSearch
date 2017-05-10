package kafka.search.consumer

import java.text.SimpleDateFormat
import java.util.{Date, Properties}

import kafka.search.common.Utils
import kafka.search.io.file.FileReader
import kafka.search.io.{Reader, Writer}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.WakeupException
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

/**
  * Created by srastogi on 28-Feb-17.
  */
class ConsumerLoop(id: Int, topics: List[String], consumerProperties: Map[String,String], writer: Writer) extends Runnable{

  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val defaultKeyLength: Int = 16

  // Define properties
  val keyRegex = consumerProperties("key.regex").r
  val extractGroups: Boolean = consumerProperties.getOrElse("regex.extract.groups", "false").toLowerCase match{
    case "true" => true
    case "false" => false
  }
  val props: Properties = new Properties()
  props.put("bootstrap.servers", consumerProperties("bootstrap.servers"))
  props.put("group.id", consumerProperties("group.id"))
  props.put("key.deserializer", consumerProperties("key.deserializer"))
  props.put("value.deserializer", consumerProperties("value.deserializer"))
  props.put("session.timeout.ms", consumerProperties("session.timeout.ms"))

  // Create Kafka Consumer
  val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
  val persistValue: Boolean = consumerProperties.getOrElse("save.complete.message", "true").toLowerCase match{
    case "true" => true
    case "false" => false
    case other: String =>
      logger.warn("Found incorrect value - '" + other + "' for property `save.complete.message`. Setting to default (TRUE).")
      true
  }

  // Override run method of Runnable
  override def run(): Unit = {
    try{
      consumer.subscribe(topics)
//      setConsumerOffset()
      while(true){
        val records: ConsumerRecords[String, String] = consumer.poll(Long.MaxValue)
        for (record <- records){
          val partition = record.partition()
          val offset = record.offset()
          val value = record.value()
          logger.debug(id + " :>> " + "Partition: " + partition + ", Offset: " + offset + ", Value: " + value)
          // Find out the key
          val key: String = {
            if(extractGroups) value match{
              case keyRegex(all @ _*) =>
                val k = all mkString "/"
                logger.debug("Key generated: " + k)
                k
              case _ => getDefaultKey(value)
            } else keyRegex findFirstIn value match {
              case Some(k) =>
                logger.debug("Key generated: " + k)
                k
              case None => getDefaultKey(value)
            }
          }
          // Persist the value in the DB
          if(persistValue) writeKeyValue(key, value)
          else writeKeyValue(key, partition.toString + "->" + offset.toString)
        }
      }
    } catch {
      case we: WakeupException => // Ignore for shutdown
    } finally {
      consumer.close()
    }
  }

  def writeKeyValue[A,B](key: A, value: B) = {
    logger.trace("Writing (key,value): " + (key.toString, value.toString))
    writer.write_key_value(key, value)
  }

  def write[A](key: A) = {
    logger.trace("Writing message: " + key.toString)
    writer.write(key)
  }

  val reader: Reader = new FileReader(consumerProperties)

  def setConsumerOffset(): Unit = {
    consumerProperties.get("offset.control").map(_.toLowerCase) match {
      case Some("beginning") =>
        logger.info("Consumer stared consuming from beginning.")
        consumer.seekToBeginning()
      case Some("end") =>
        logger.info("Consumer stared consuming from end.")
        consumer.seekToEnd()
      case Some("specific") =>
        logger.info("Consumer starting from specific partitions.")
        Utils.configReader(consumerProperties("offset.file")).foreach{
          case (topic, data) =>
            logger.info("Consumer started consuming topic: " + topic + ", partition: " + data.split(",")(0) + " from offset: " + data.split(",")(1))
            consumer.seek(new TopicPartition(topic, data.split(",")(0).toInt), data.split(",")(1).toLong)
        }
      case None => logger.info("Not received any value for offset.control. It will use the offsets which is saved in Zookeeper for the consumer's group id.")
      case _ => logger.info("Incorrect value entered for offset.control. It will use the offsets which is saved in Zookeeper for the consumer's group id.")
    }
  }

  /**
    * Extracts and returns first defaultKeyLength characters from value.
    * */
  def getDefaultKey(value: String): String = {
    logger.debug("Selecting default (first "+defaultKeyLength+" characters).")
    value.substring(0, defaultKeyLength-1)
  }

  // Shutdown
  def shutdown() = consumer.wakeup()

}
