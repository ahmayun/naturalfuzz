package fuzzer

import runners.Config


//depsInfo: [[(ds, col, row), (ds, col, row)], [(ds, col, row)] .... [(ds, col, row)]]
class ProvInfo(val depsInfo: Array[Array[(Int,Int,Int)]]) {

  def this() = {
    this(Array())
  }

  def getLocs(): Array[Array[(Int,Int,Int)]] = { depsInfo }

  def updateRowSet(newLocs: Map[(Int, Int), (Int, Int)]): ProvInfo = {
    new ProvInfo(depsInfo.map(
      _.map {
        case (ds, col, row) =>
          val Some((newDs, newRow)) = newLocs.get((ds, row))
          (newDs, col, newRow)
      }
    ))
  }

  def getRowLevelProvenance(): List[(Int,Int)] = {
    depsInfo.last.map{case (ds, _, row) => (ds, row)}.distinct.toList
  }

  def merge(): ProvInfo = {
    new ProvInfo(Array(depsInfo.flatten))
  }

  def append(other: ProvInfo): ProvInfo = {
    new ProvInfo(depsInfo ++ other.depsInfo)
  }

  def getRandom(): ProvInfo = {
    new ProvInfo(Array(depsInfo.last)) // TODO IMPORTANT: MAKE RANDOM
  }

  def getCoDependentRegions(): Array[Array[(Int,Int,Int)]] = { depsInfo }

  def simplify(): ProvInfo = {
    new ProvInfo(Config.benchmarkName match {
      case "WebpageSegmentation" =>
        Array(
          Array(0,1).flatMap(d => Array(0,5,6).map(c => (d, c, 0)))
        )
      case _ => depsInfo
    })

  }

  override def toString: String = {
    depsInfo.map(_.map{case (ds, row, col) => s"($ds,$row,$col)"}.mkString("<=>")).mkString("\n----------------------------\n")
  }
}
