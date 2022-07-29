package jazzer

import com.code_intelligence.jazzer.api.FuzzedDataProvider
import examples.fuzzable.CommuteType

object JazzerTargetCommuteType {

  def fuzzerInitialize(args: Array[String]): Unit = {
    args.foreach{s => println(s"arg: $s")}
  }

  def fuzzerTestOneInput(data: FuzzedDataProvider): Unit = {
    val datasets: Array[String] = Array[String]("/seeds/weak_seed/commute/trips")
    val newDatasets: Array[String] = SharedJazzerLogic.createMutatedDatasets(data, datasets)
    CommuteType.main(newDatasets)
    // Might need to manipulate scoverage measurement files produced by execution
    // since the old one will be overridden on next call or to indicate sequence
    // maybe attach iteration number to it

    throw new Exception() // How to change the termination condition (jazzer stops when it finds a bug)
  }

  def main(args: Array[String]): Unit = {
    System.out.println("<=== JazzerTargetCommuteType.main() ===>")
    CommuteType.main(Array[String]("seeds/weak_seed/commute/trips"))
  }
}
