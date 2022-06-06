package utils

import abstraction.{SparkConf, SparkContext}
import symbolicexecution.SymExResult

class FilterQueries(val pathExpressions: SymExResult) {
  def getRows(datasets: Array[String]): QueriedRDDs = {
    val sc = new SparkContext(new SparkConf())
    datasets.map(ds => sc.textFile(ds))
    new QueriedRDDs()
  }

}
