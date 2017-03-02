package kafka.search.io

/**
  * Created by srastogi on 01-Mar-17.
  */
trait Writer {

  def write_key_value[A, B](key: A, value: B): Boolean

  def write[A](msg: A): Boolean = {
    write_key_value[A, String](msg, "\\-\\-\\")
  }

  def close(): Unit

}
