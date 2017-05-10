package kafka.search.io.file.disk.ops

import java.io.RandomAccessFile
import java.nio.channels.FileChannel

/**
  * Created by srastogi on 17-Mar-17.
  * File contains 2^k buckets, each 256 bytes or 32 entries of 8 bytes
  * Empty entries are just zeroed out (000... is a valid hash, but I don't care about 2^-64 chance of collision, if everything can collide with everything else already, by the nature of hashing).
  * Every hash resides in bucket guessed via its first k bits
  * If any bucket overflows, double file size and split every bucket
  * Everything is accessed via mmap(), not read()/write()
  */
class MemoryMappedFile() {

  val length: Int = 0x8FFFFFF // 128MB

  new RandomAccessFile("mmapFile.dat", "rw")
    .getChannel
    .map(FileChannel.MapMode.READ_WRITE, 0, length)

}
