package utils

import abstraction.BaseRDD

class QueriedRDDs(val filteredRDDs: List[QueryResult]) {
  def mixMatch(): QueryResult = {
    filteredRDDs.foldLeft(filteredRDDs.head){case (acc, e) => acc.mixMatchQueryResult(e, "random")}
  }

}
