package utils

import abstraction.BaseRDD
import runners.Config
import utils.MutationUtils.flipCoin

class QueryResult(val filterQueryRDDs: Array[BaseRDD[String]], val query: Seq[Query], val locs: RDDLocations) extends Serializable {

  def replaceCols(cols: Array[String], rdd: BaseRDD[String], locs: RDDLocations, ds: Int): Array[String] = {
    if (flipCoin(Config.dropMixProb))
      return cols

    val colsRep = rdd.takeSample(false, 1).head.split(Config.delimiter)
    val colsIdx = locs.getCols(ds)
    cols
      .zipWithIndex
      .map{case (c, i) => if(colsIdx.contains(i) && !flipCoin(Config.keepColProb)) colsRep(i) else c}
  }

  def mixMatchRDD(rdd1: BaseRDD[String], rddAndLocs: (BaseRDD[String], RDDLocations), ds: Int): BaseRDD[String] = {
    val (rdd, locs) = rddAndLocs
    rdd1.map{
      row =>
        val cols = row.split(Config.delimiter)
        replaceCols(cols, rdd, locs, ds).mkString(",")
    }
  }

  def mixMatchRDDs(rdds1: Array[BaseRDD[String]], queryResult: QueryResult): QueryResult = {
    new QueryResult(rdds1.zipWithIndex.zip(queryResult.filterQueryRDDs).map{
      case ((rdd1, dsi), rdd2) =>
        if(rdd1.data.isEmpty) rdd2
        else if(rdd2.data.isEmpty) rdd1
        else mixMatchRDD(rdd1, (rdd2, queryResult.locs), dsi)
    }, query, locs) //TODO: combine query info and locs info, currently only the first one is propogated
  }

  def mixMatchQueryResult(qr: QueryResult, setting: String): QueryResult = {
    setting.toLowerCase match {
      case "random" => mixMatchRDDs(filterQueryRDDs, qr)
    }
  }

  override def toString: String = {
    println("locs", locs)
    println("length of query", query.length)
    filterQueryRDDs.mkString("\n===============\n")
  }
}
