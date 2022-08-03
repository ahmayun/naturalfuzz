package jazzer

import com.code_intelligence.jazzer.api.FuzzedDataProvider
import fuzzer.Schema
import scoverage.Platform.FileWriter
import scoverage.Serializer
import utils.{FileUtils, IOUtils}

import java.io.File

object SharedJazzerLogic {

  var i = 0
  var prevCov = 0.0

  def fuzzTestOneInput(
                        data: FuzzedDataProvider,
                        f: Array[String] => Unit,
                        mode: String,
                        measurementsDir: String,
                        datasets: Array[String],
                        schema: Array[Array[Schema[Any]]]): Unit = {

    val newDatasets: Array[String] = if (mode.equals("use_schema"))
      SharedJazzerLogic.createMutatedDatasets(data, datasets, schema)
    else
      SharedJazzerLogic.createMutatedDatasets(data, datasets, Array())

    i+=1
    var throwable: Throwable = null
    try { f(newDatasets) } 
    catch { case e: Throwable => throwable = e } 
    finally {
      SharedJazzerLogic.trackCumulativeCoverage(measurementsDir)
    }

    if (throwable == null)
      throw throwable
  }

  def trackCumulativeCoverage(measurementsDir: String): Unit = {
    val coverage = Serializer.deserialize(new File(s"$measurementsDir/scoverage.coverage")) // scoverage.coverage will be produced at compiler time by ScoverageInstrumenter.scala
    val measurementFiles = IOUtils.findMeasurementFiles(measurementsDir)
    val measurements = scoverage.IOUtils.invoked(measurementFiles)
    coverage.apply(measurements)
    if(coverage.statementCoveragePercent > prevCov) {
      new FileWriter(new File(s"$measurementsDir/cumulative.csv"), true)
          .append(s"$i,${coverage.statementCoveragePercent}")
          .append("\n")
          .flush()
      prevCov = coverage.statementCoveragePercent
    }
  }

  def createMeasurementDir(path: String): Unit = {
    new File(path).mkdirs()
  }


  def createMutatedDatasets(provider: FuzzedDataProvider, datasets: Array[String], schemas: Array[Array[Schema[Any]]]): Array[String] = {
    if (schemas.nonEmpty) {
      datasets.zip(schemas).map{ case (path, schema) => createMutatedDatasetSchemaAware(provider, path, schema) }
    } else {
      datasets.map{ path => createMutatedDataset(provider, path) }
    }
  }

  def createMutatedDatasetSchemaAware(provider: FuzzedDataProvider, path: String, schema: Array[Schema[Any]]): String = {
    val data = provider.consumeRemainingAsAsciiString().split("\n")
    FileUtils.writeToFile(data.toSeq, s"$path/part-00000")
    path
  }

  def createMutatedDataset(provider: FuzzedDataProvider, path: String): String = {
    val data = provider.consumeRemainingAsAsciiString().split("\n")
    FileUtils.writeToFile(data.toSeq, s"$path/part-00000")
    path
  }
}
