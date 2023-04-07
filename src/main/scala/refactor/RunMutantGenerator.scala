package refactor

import refactor.CodeTransformUtils.{treeFromFile,writeTransformed}
import java.io.File
import scala.meta._

object RunMutantGenerator {
  def main(args: Array[String]): Unit = {
    val inputFolder = "src/main/scala/examples/faulty"
    val testName = "WebpageSegmentation"
    val outputFolder = s"src/main/scala/examples/mutants/${testName}"
    new File(outputFolder).mkdirs()
    val inputFile = s"$inputFolder/$testName.scala"
    val tree = treeFromFile(inputFile)


    val mutants = MutantGenerator.generateMutants(tree, testName)
    mutants
      .zipWithIndex
      .foreach {
      case ((transformed, mutantSuffix), i) =>
        writeTransformed(transformed.toString(), s"$outputFolder/${testName}_$mutantSuffix.scala")
    }

  }
}
