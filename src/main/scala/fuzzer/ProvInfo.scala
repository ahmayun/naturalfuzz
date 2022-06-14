package fuzzer

import provenance.data.Provenance
import runners.Config

import scala.collection.mutable.ListBuffer


//depsInfo: [[(ds, col, row), (ds, col, row)], [(ds, col, row)] .... [(ds, col, row)]]
class ProvInfo(val depsInfo: ListBuffer[ListBuffer[(Int,Int,Int)]]) {
  def update(id: Int, provenances: ListBuffer[Provenance]): Unit = {
    depsInfo.append(provenances.flatMap(_.convertToTuples))
  }

  def this() = {
    this(ListBuffer())
  }

  def getLocs(): ListBuffer[ListBuffer[(Int,Int,Int)]] = { depsInfo }

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
    new ProvInfo(ListBuffer(depsInfo.flatten))
  }

  def append(other: ProvInfo): ProvInfo = {
    new ProvInfo(depsInfo ++ other.depsInfo)
  }

  def getRandom(): ProvInfo = {
    new ProvInfo(ListBuffer(depsInfo.last)) // TODO IMPORTANT: MAKE RANDOM
  }

  def getCoDependentRegions(): ListBuffer[ListBuffer[(Int,Int,Int)]] = { depsInfo }

  def simplify(): ProvInfo = {
    new ProvInfo(Config.benchmarkName match {
      case "WebpageSegmentation" =>
        ListBuffer(
          ListBuffer(0,1).flatMap(d => ListBuffer(0,5,6).map(c => (d, c, 0)))
        )
      case _ => depsInfo
    })

  }

  override def toString: String = {
    depsInfo.map(_.map{case (ds, row, col) => s"($ds,$row,$col)"}.mkString("<=>")).mkString("\n----------------------------\n")
  }
}
