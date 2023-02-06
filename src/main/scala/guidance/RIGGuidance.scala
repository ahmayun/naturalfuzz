package guidance

import fuzzer.{Global, Guidance, Schema}
import scoverage.Coverage
import scoverage.Platform.FileWriter
import utils.QueriedRDDs

import java.io.File
import scala.concurrent.duration.DurationInt

class RIGGuidance(
                   val inputFiles: Array[String],
                   val schemas: Array[Array[Schema[Any]]],
                   val duration: Int,
                   val qrdds: QueriedRDDs) extends Guidance {
  var last_input = inputFiles
  var coverage: Coverage = new Coverage
  val deadline = duration.seconds.fromNow
  var runs = 0

  def mutate(inputDatasets: Array[Seq[String]]): Array[Seq[String]] = {
    val mm = qrdds.mixMatch(inputDatasets).filterQueryRDDs.map(_.data)
    println("original")
    inputDatasets(0).foreach(println)
    println("")
    println("mutated")
    mm(0).foreach(println)
    println("")
    mm
  }

  override def getInput(): Array[String] = {
    last_input
  }

  override def isDone(): Boolean = {
    !deadline.hasTimeLeft()
  }

  override def updateCoverage(cov: Coverage, outDir: String = "/dev/null", crashed: Boolean = true): Boolean = {
    if(Global.iteration == 0 || cov.statementCoveragePercent > this.coverage.statementCoveragePercent) {
      this.coverage = cov
      new FileWriter(new File(s"$outDir/cumulative.csv"), true)
        .append(s"${Global.iteration},${coverage.statementCoveragePercent}")
        .append("\n")
        .flush()
    }
    true
  }
}
