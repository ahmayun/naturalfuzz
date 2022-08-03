package jazzer

import com.code_intelligence.jazzer.api.FuzzedDataProvider
import fuzzer.Schema
import utils.FileUtils

import java.io.File

object SharedJazzerLogic {

  var i = 0
  def renameMeasurementsFile(measurementsDir: String): Unit = {
    val dir = new File(measurementsDir)
    val file = dir
      .listFiles
      .filter(_.isFile)
      .filter(_.toString.contains("scoverage.measurements.0"))(0)

    new File(file.toString).renameTo(new File(s"${file.getParentFile}/scoverage.measurements.$i"))
    i+=1
  }

  def createMeasurementDir(path: String): Unit = {
    val success = new File(path).mkdirs()
    if(success)
      println(s"successfully created $path")
    else
      println(s"failed to create $path")
  }


  def createMutatedDatasets(provider: FuzzedDataProvider, datasets: Array[String], schemas: Array[Array[Schema[Any]]]): Array[String] = {
    println(s"createMutatedDatasets() - nBytes: ${provider.remainingBytes()}")

    if (schemas.nonEmpty) {
      datasets.zip(schemas).map{ case (path, schema) => createMutatedDatasetSchemaAware(provider, path, schema) }
    } else {
      datasets.map{ path => createMutatedDataset(provider, path) }
    }
  }

  def createMutatedDatasetSchemaAware(provider: FuzzedDataProvider, path: String, schema: Array[Schema[Any]]): String = {
    val data = provider.consumeRemainingAsAsciiString().split("\n")
//    println(s"==DATA: $path==")
//    println(data.mkString("\n"))
//    println("================")
    FileUtils.writeToFile(data.toSeq, s"$path/part-00000")
    path
  }

  def createMutatedDataset(provider: FuzzedDataProvider, path: String): String = {
    val data = provider.consumeRemainingAsAsciiString().split("\n")
    FileUtils.writeToFile(data.toSeq, s"$path/part-00000")
    path
  }
}
