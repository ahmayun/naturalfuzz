package jazzer

import com.code_intelligence.jazzer.api.FuzzedDataProvider
import examples.fuzzable.WebpageSegmentation
import schemas.BenchmarkSchemas

object JazzerTargetWebpageSegmentation {

  val datasets: Array[String] = Array(
    "/inputs/ds1",
    "/inputs/ds2"
  )

  var mode: String = ""
  var measurementsDir: String = ""

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
    val newDatasets: Array[String] = if (mode.equals("use_schema"))
      SharedJazzerLogic.createMutatedDatasets(data, datasets, BenchmarkSchemas.SEGMENTATION)
    else
      SharedJazzerLogic.createMutatedDatasets(data, datasets, Array())

    var throwable: Throwable = null
    try {
      WebpageSegmentation.main(newDatasets)
    } catch {
      case e => throwable = e
    } finally {
      SharedJazzerLogic.renameMeasurementsFile(measurementsDir)
      SharedJazzerLogic.trackCumulativeCoverage(measurementsDir)
    }

    if (throwable == null) {
      throw throwable
    }
  }

  def main(args: Array[String]): Unit = {
    System.out.println("<=== JazzerTargetCommuteType.main() ===>")
  }
}
