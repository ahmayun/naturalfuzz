package refactor

import refactor.CodeTransformUtils.{treeFromFile,writeTransformed}
import java.io.File
import scala.meta._

object RunMutantGenerator {
  def main(args: Array[String]): Unit = {
    val outputFolder = "src/main/scala/examples/mutants"
    val inputFolder = "src/main/scala/examples/faulty"
    val testName = "WebpageSegmentation"
    new File(outputFolder).mkdirs()
    val inputFile = s"$inputFolder/$testName.scala"

    println("-"*3 + s" $testName " + "-"*10)
    val tree = treeFromFile(inputFile)
    val mutants = MutantGenerator.generateMutants(tree)
    println(tree.structure)
    mutants.foreach {
      case (transformed, mutantName) =>
        writeTransformed(transformed.toString(), s"$outputFolder/${testName}_$mutantName.scala")
    }

  }
}
