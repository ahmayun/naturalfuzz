package utils

import abstraction.BaseRDD

class Query(val queryFunc: Array[BaseRDD[String]] => Array[BaseRDD[String]], val locs: RDDLocations) {
  def runQuery(rdds: Array[BaseRDD[String]]): QueryResult = {
    new QueryResult(queryFunc(rdds), Seq(), locs)
  }
}
