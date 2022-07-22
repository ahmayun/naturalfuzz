package utils

class RDDLocations(val locs: Array[(Int, Int, Int)]) {

  val dsCols: Map[(Int, Int), Array[Int]] = locs
    .groupBy{case (ds, col, _) => (ds, col)}
    .mapValues(arr => arr.map{case (_, _, r) => r})

  def getCols(dsi: Int): Array[Int] = {
    dsCols
      .filter{case ((ds, _), _) => dsi == ds}
      .map{case ((_, col), _) => col}
      .toArray
  }

  override def toString: String = locs.map{case (ds,col,row) => s"($ds,$col,$row)"}.mkString("|")

}
