package jazzer

import com.code_intelligence.jazzer.api.FuzzedDataProvider
import examples.fuzzable.CommuteType
import schemas.BenchmarkSchemas

object JazzerTargetCommuteType {

  val datasets: Array[String] = Array(
    "/inputs/trips/part-00000"
  )

  def fuzzerInitialize(args: Array[String]): Unit = {
    println(s"JAZZER ARGS: ${args.mkString(", ")}")
    SharedJazzerLogic.createMeasurementDir(args(0))
  }

  def fuzzerTestOneInput(data: FuzzedDataProvider): Unit = {
    val newDatasets: Array[String] = SharedJazzerLogic.createMutatedDatasets(data, datasets, BenchmarkSchemas.COMMUTE)
    CommuteType.main(newDatasets)
  }

  def main(args: Array[String]): Unit = {
    System.out.println("<=== JazzerTargetCommuteType.main() ===>")
    CommuteType.main(Array[String]("seeds/weak_seed/commute/trips"))
  }
}
