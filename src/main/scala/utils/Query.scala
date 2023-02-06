package utils

import abstraction.BaseRDD
import symbolicexecution.SymbolicTree

class Query(val queryFunc: Array[BaseRDD[String]] => Array[BaseRDD[String]], val locs: RDDLocations, val tree: SymbolicTree) extends Serializable {
  def offsetLocs(ds: Int, length: Int): Query = {
    new Query(queryFunc, locs.offsetLocs(ds, length), tree.offsetLocs(ds, length))
  }

  def runQuery(rdds: Array[BaseRDD[String]]): QueryResult = {
    new QueryResult(queryFunc(rdds), Seq(), locs)
  }

  override def hashCode(): Int = {
    tree.hashCode()
  }

  override def equals(obj: Any): Boolean = {
    obj.hashCode() == this.hashCode()
  }

  def involvesDS(ds: Int): Boolean = {
    locs.involvesDS(ds)
  }

  def isMultiDatasetQuery: Boolean = {
    locs.isMultiDataset
  }

  def isDummyQuery: Boolean = tree == null

}

object Query {
  def dummyQuery(): Query = {
    new Query(rdds => rdds, new RDDLocations(Array()), SymbolicTree.nopTree())
  }
}
