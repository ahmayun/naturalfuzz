package utils

class RDDLocations(val locs: Array[(Int, Int, Int)]) extends Serializable {
  def offsetLocs(ds: Int, offset: Int): RDDLocations = {
    new RDDLocations(locs.map {
      case loc @ (_ds, col, row) =>
        if (ds == _ds) {
          (ds, col+offset,row)
        } else {
          loc
        }
    })
  }


  val dsCols: Map[(Int, Int), Array[Int]] = locs
    .groupBy{case (ds, col, _) => (ds, col)}
    .mapValues(arr => arr.map{case (_, _, r) => r})
    .map(identity)

  def getCols(dsi: Int): Array[Int] = {
    dsCols
      .filter{case ((ds, _), _) => dsi == ds}
      .map{case ((_, col), _) => col}
      .toArray
  }

  def involvesDS(ds: Int): Boolean = {
    locs.exists {case (d, _, _) => d == ds}
  }

  override def toString: String = locs.map{case (ds,col,row) => s"($ds,$col,$row)"}.mkString("|")

  def isMultiDataset: Boolean = {
    locs.map { case (ds, _, _) => ds}.distinct.length > 1
  }
}
