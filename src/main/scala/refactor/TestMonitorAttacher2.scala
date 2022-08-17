package refactor

import utils.FileUtils

import java.io.{BufferedReader, File}
import scala.meta._

object TestMonitorAttacher2 {

  def main(args: Array[String]): Unit = {
    val outputFolder = "src/main/scala/refactor/testout"
    new File(outputFolder).mkdirs()
    val testName = "WordCount"
    val testData = FileUtils.readFile("src/main/scala/examples/clustermonitor/WordCount.scala").mkString("\n")

    println(testData)
    println("-"*3 + s" $testName " + "-"*10)
    val outputFile = s"$outputFolder/$testName.scala"
    val tree = testData.parse[Source].get
    val transformed = MonitorAttacher.attachMonitors(tree)
    println(tree.structure)
    println(transformed.structure)
    MonitorAttacher.writeTransformed(transformed.toString(), outputFile)
  }
}