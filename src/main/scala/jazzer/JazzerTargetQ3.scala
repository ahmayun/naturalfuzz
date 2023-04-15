package jazzer

import com.code_intelligence.jazzer.api.FuzzedDataProvider

object JazzerTargetQ3 {

  var mode: String = ""
  var pkg: String = ""
  var measurementsDir: String = ""
  var mutantName = ""
  val datasets: Array[String] = Array(
    "/inputs/ds1",
    "/inputs/ds2",
    "/inputs/ds3"
  )

  def fuzzerInitialize(args: Array[String]): Unit = {
    measurementsDir = args(0)
    mode = args(1)
    pkg = args(2)
    mutantName = args(3)

    SharedJazzerLogic.t_start = System.currentTimeMillis()
    SharedJazzerLogic.createMeasurementDir(measurementsDir)
  }

  def fuzzerTestOneInput(data: FuzzedDataProvider): Unit = {
    // Might need to manipulate scoverage measurement files produced by execution
    // since the old one will be overridden (P.S. not true) on next call or to indicate sequence
    // maybe attach iteration number to it

    // Schema ds1 & ds2: string,int,int,int,int,int,string


    val f: Array[String] => Unit = pkg match {
      case "faulty" => examples.faulty.Q3.main
    }

    mode match {
      case "reproduce" => SharedJazzerLogic.fuzzTestOneInput(
        data,
        datasets,
        f
      )
      case "fuzz" => SharedJazzerLogic.fuzzTestOneInput(
        data,
        f,
        measurementsDir,
        datasets
      )
    }
  }

}