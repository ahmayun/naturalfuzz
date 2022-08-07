package testfiles

import fuzzer.ProvInfo
import scoverage.Platform.FileWriter

import java.io.File
import scala.collection.mutable.ListBuffer

object Test3 {

  def main(args: Array[String]): Unit = {
    new FileWriter(new File("randomtest"))
  }
}
