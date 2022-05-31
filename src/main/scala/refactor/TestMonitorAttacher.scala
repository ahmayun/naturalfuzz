package refactor

import java.io.File
import scala.meta._

object TestMonitorAttacher {

  def main(args: Array[String]): Unit = {
    val outputFolder = "src/main/scala/refactor/testout"
    new File(outputFolder).mkdirs()

    TestCases.testCases.foreach { case (testName, testData) =>
      println("-"*3 + s" $testName " + "-"*10)
      val outputFile = s"$outputFolder/$testName.scala"
      val tree = testData.parse[Source].get
      val transformed = MonitorAttacher.attachMonitors(tree)
      MonitorAttacher.writeTransformed(transformed.toString(), outputFile)
    }
  }
}