package guidance

import fuzzer.{Global, Guidance, ProvInfo, Schema}
import scoverage.Coverage
import utils.{InputMetaData, QueriedRDDs}

class RIGGuidance(
                   val inputFiles: Array[String],
                   val schemas: Array[Array[Schema[Any]]],
                   val maxRuns: Int,
                   val qrdds: QueriedRDDs) extends Guidance {
  var last_input = inputFiles
  var coverage: Coverage = new Coverage
  var runs = 0

  def mutate(inputDatasets: Array[Seq[String]]): Array[Seq[String]] = {
    qrdds.mixMatch().filterQueryRDDs.map(_.data)
  }

  override def getInput(): Array[String] = {
    last_input
  }

  override def isDone(): Boolean = {
    Global.iteration >= this.maxRuns
  }

  override def updateCoverage(coverage: Coverage, crashed: Boolean = true): Boolean = {
    if(Global.iteration != 0 && coverage.statementCoveragePercent <= this.coverage.statementCoveragePercent && !crashed) {
      return true
    }
    this.coverage = coverage
    true
  }
}
