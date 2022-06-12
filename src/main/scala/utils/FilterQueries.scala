package utils

import abstraction.{SparkConf, SparkContext}
import symbolicexecution.SymExResult

class FilterQueries(val symExResult: SymExResult) {

  val filterQueries = symExResult.getPathQueriesFromPathExpression
  def getRows(datasets: Array[String]): List[QueryResult] = {
    val sc = new SparkContext(null)
    val rdds = datasets.map(sc.textFile)
    filterQueries.map(fq => fq.runQuery(rdds))
  }

}
