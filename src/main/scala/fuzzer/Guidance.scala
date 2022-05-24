package fuzzer

import scoverage.Coverage

trait Guidance {
  def mutate(inputDatasets: Array[Seq[String]]): Array[Seq[String]]

  def updateCoverage(coverage: Coverage, crashed: Boolean = true): Boolean

  def getInput(): Array[String]

  def isDone(): Boolean
}
