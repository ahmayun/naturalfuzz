package utils

import abstraction.BaseRDD

class QueriedRDDs(val filteredRDDs: List[QueryResult]) {
  def mixMatch(inputDatasets: Array[Seq[String]]): QueryResult = {
    val inputToQR = new QueryResult(inputDatasets.map(ds => new BaseRDD[String](ds)),Seq(), new RDDLocations(Array()))
    filteredRDDs.foldLeft(inputToQR){case (acc, e) => acc.mixMatchQueryResult(e, "random")}
  }

}
