package jazzer

import com.code_intelligence.jazzer.api.FuzzedDataProvider
import examples.fuzzable.WebpageSegmentation

object JazzerTargetWebpageSegmentation {

  def fuzzerInitialize(args: Array[String]): Unit = {
    println(s"JAZZER ARGS: ${args.mkString(", ")}")
    SharedJazzerLogic.createMeasurementDir(args(0))
  }

  def fuzzerTestOneInput(data: FuzzedDataProvider): Unit = {
    val datasets: Array[String] = Array("/seeds/weak_seed/webpage_segmentation/before", "/seeds/weak_seed/webpage_segmentation/after")
    val newDatasets: Array[String] = SharedJazzerLogic.createMutatedDatasets(data, datasets)
    WebpageSegmentation.main(newDatasets)
    // Might need to manipulate scoverage measurement files produced by execution
    // since the old one will be overridden on next call or to indicate sequence
    // maybe attach iteration number to it

    throw new Exception() // How to change the termination condition (jazzer stops when it finds a bug)
  }

  def main(args: Array[String]): Unit = {
    System.out.println("<=== JazzerTargetCommuteType.main() ===>")
  }
}
