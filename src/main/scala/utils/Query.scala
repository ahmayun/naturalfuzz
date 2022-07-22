package utils

import abstraction.BaseRDD
import symbolicexecution.SymbolicTree

class Query(val queryFunc: Array[BaseRDD[String]] => Array[BaseRDD[String]], val locs: RDDLocations, val tree: SymbolicTree) {
  def runQuery(rdds: Array[BaseRDD[String]]): QueryResult = {
    new QueryResult(queryFunc(rdds), Seq(), locs)
  }
}
