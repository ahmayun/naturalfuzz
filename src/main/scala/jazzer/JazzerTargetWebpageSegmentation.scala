package jazzer

import com.code_intelligence.jazzer.api.FuzzedDataProvider
import examples.fuzzable.WebpageSegmentation
import schemas.BenchmarkSchemas
import scoverage.Platform.FileWriter

import java.io.File

object JazzerTargetWebpageSegmentation {

  var mode: String = ""
  var measurementsDir: String = ""
  val datasets: Array[String] = Array(
    "/inputs/ds1",
    "/inputs/ds2"
  )

  def fuzzerInitialize(args: Array[String]): Unit = {
    mode = if(args.length > 1) args(1) else ""
    measurementsDir = args(0)

    println(s"JAZZER ARGS: ${args.mkString(", ")}")
    SharedJazzerLogic.createMeasurementDir(measurementsDir)
  }

  def fuzzerTestOneInput(data: FuzzedDataProvider): Unit = {
    // Might need to manipulate scoverage measurement files produced by execution
    // since the old one will be overridden (P.S. not true) on next call or to indicate sequence
    // maybe attach iteration number to it

    // Schema ds1 & ds2: string,int,int,int,int,int,string
    SharedJazzerLogic.fuzzTestOneInput(data, WebpageSegmentation.main, mode, measurementsDir, datasets, BenchmarkSchemas.SEGMENTATION)
  }


}
