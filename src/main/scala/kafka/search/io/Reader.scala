package kafka.search.io

/**
  * Created by srastogi on 02-Mar-17.
  */
trait Reader {

  // Function to get the value based on the key
  def get[A](key: A): Option[String] // TODO: Return a generic type rather than just String

  def contains[A](key: A): Boolean

  def close(): Unit
}
