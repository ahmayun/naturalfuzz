package utils

import abstraction.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import runners.Config
import symbolicexecution.SymExResult
import utils.MiscUtils.toBinaryStringWithLeadingZeros

class FilterQueries(val symExResult: SymExResult) extends Serializable {

  val filterQueries: List[Query] = symExResult.getPathQueries.distinct

  def filterPiecesForDS(i: Int): List[Query] = {
    filterQueries.filter(q => q.locs.involvesDS(i))
  }

  def getRows(datasets: Array[String]): List[QueryResult] = {
    val sc = new SparkContext(null)
    val rdds = datasets.map(sc.textFile)
    filterQueries.map(fq => fq.runQuery(rdds))
  }

  def createSatVectors(rdds: Array[RDD[String]]): Array[RDD[(String, Int)]] = {
//    val sc = new SparkContext(null)
//    val rdds = datasets.map(sc.textFile)
    println("Pre Join filling started...")
    val satRDD = rdds
      .zipWithIndex
      .map {
        case (rdd, i) => {
          rdd.map {
            row =>
              val filterQueriesDS = filterQueries
                .map {
                  q =>
                    if (q.involvesDS(i)) q else Query.dummyQuery()
                }
              val filterFns = filterQueriesDS
                .map(_.tree.createFilterFn(Array(i)))
              val cols = row.split(Config.delimiter)
              val sat = makeSatVector(filterFns.zip(filterQueriesDS), cols)
              (cols.mkString(Config.delimiter), sat)
          }
        }
      }
    satRDD
  }

  def createSatVectors(
                        rdds: Array[RDD[((String, Int), Long)]],
                        savedJoins: Array[(RDD[(String, ((String, Long), (String, Long)))], Int, Int)]
                      ): Array[RDD[((String, Int), Long)]] =
  {

    println("Post Join filling started...")
    val savedJoinsWithPV = savedJoins.map { // j is the joined dataset, a and b are dataset ids of joined datasets
      case (rdd, dsA, dsB) =>
        (rdd.map {
          case (k, ((rowA, rowAi), (rowB, rowBi))) =>
            val combinedRow = s"$rowA,$rowB"
            val filterQueriesDS = filterQueries
              .map {
                q =>
                  if (q.involvesDS(dsA) && q.involvesDS(dsB)) {
                    q
                  } else {
                    Query.dummyQuery()
                  }
              }
            val ds2Offset = rowA.split(Config.delimiter).length
            val filterFns = filterQueriesDS
              .map {q => q.offsetLocs(dsB, ds2Offset)}
              .map(_.tree.createFilterFn(Array(dsA, dsB), ds2Offset))
            val pv = makeSatVector(filterFns.zip(filterQueriesDS), combinedRow.split(","), true)
            println(s"jrow: $combinedRow - ${toBinaryStringWithLeadingZeros(pv)}")
            (k, ((rowA, rowAi, pv), (rowB, rowBi, pv)))
        }, dsA, dsB)
    }

    val transformed = savedJoinsWithPV.flatMap {
      case (rdd, dsA, dsB) =>
        Array(
          (rdd.map {
            case (_, ((rowA, rowAi, pv1), _)) =>
              ((dsA,rowAi), (rowA, pv1))
          }, dsA),
          (rdd.map {
            case (_, (_, (rowB, rowBi, pv2))) =>
              ((dsB,rowBi), (rowB, pv2))
          }, dsB)
        )
    }

    val filtered = rdds
      .indices
      .map {
        i =>
          transformed.filter { case (_, ds) => i == ds}
      }

    val transformedInputRDDs = rdds
      .zipWithIndex
      .map {
        case (rdd, ds) =>
          rdd.map {
            case (row, rowi) =>
              ((ds, rowi), row)
          }
      }

    val finalPathVectors = transformedInputRDDs
      .zipWithIndex
      .map {
        case (rdd, i) =>
          filtered(i)
            .foldLeft(rdd) {
              case (acc, (e, _)) =>
                acc
                  .leftOuterJoin(e)
                  .map {
                    case (key, ((row, pv1), Some((_, pv2)))) =>
                      (key, (row, pv1 | pv2))
                    case (key, (left, _)) =>
                      (key, left)
                  }
            }
      }

    println("\n BEGIN PRINTING JOINS\n ")
    finalPathVectors
      .zipWithIndex
      .foreach {
        case (joined, i) =>
          println(s"JOINED $i")
          joined
            .collect()
            .foreach(println)
      }
    println("\n END PRINTING JOINS \n")

    // TODO: Final reduceByKey to merge duplicated rows after joins

    finalPathVectors
      .map {
        rdd =>
          rdd.map {
            case ((_, rowi), (row, pv)) =>
              ((row, pv), rowi)
          }
      }
//    val savedJoinsWithPV = savedJoins.map { // j is the joined dataset, a and b are dataset ids of joined datasets
//      case (rdd, dsA, dsB) =>
//        (rdd.map {
//          case (k, ((rowA, rowAi), (rowB, rowBi))) =>
//            val combinedRow = s"$rowA,$rowB"
//            val filterFns = filterQueries
//              .filter { q => q.involvesDS(dsA) && q.involvesDS(dsB) }
//              .map(_.tree.createFilterFn(Array(dsA, dsB)))
//            val pv = makeSatVector(filterFns, combinedRow.split(","))
//            (k, ((rowA, rowAi, pv), (rowB, rowBi, pv)))
//        }, dsA, dsB)
//    }
//
//    rdds.zipWithIndex.map {
//      case (rdd, dsi) =>
//        rdd.zipWithIndex.map {
//          case (row, rowi) =>
//            val newPathVector = combinePathVectorsForRow((dsi, rowi), savedJoinsWithPV)
//            modifyPathVector(row, newPathVector)
//        }
//    }
  }

  def combinePathVectorsForRow(loc: (Int, Long), savedJoinsWithPVs: Array[(RDD[(String, ((String, Long, Int), (String, Long, Int)))], Int, Int)]): Int = {
    val (ds, row) = loc
    val involvedDS = savedJoinsWithPVs
      .filter {
        case (_, dsA, dsB) =>
          dsA == ds || dsB == ds
      }
      .map {
        case (rdd, dsA, _) =>
          rdd.map {
            case (_, (rowDSA, rowDSB)) =>
              if(ds == dsA) rowDSA else rowDSB
          }
      }

    involvedDS.foldLeft(0) {
      case (acc, rdd) =>
        acc | rdd.aggregate(0)({
          case (acc, (_, rowi, pv)) =>
            if (rowi == row) acc | pv else acc
        }, {
          case (pvA, pvB) => pvA | pvB
        }
        )
    }
  }

  def makeSatVector(conditions: List[(Array[String] => Int, Query)], row: Array[String], combined: Boolean = false): Int = {
    if (conditions.length > 32 / 2)
      throw new Exception("Too many conditions, unable to encode as 32 bit integer")

    val pv = conditions
      .map {
        case (f, q) =>
          val check = !combined && q.isMultiDatasetQuery
          if (check)
            0x00
          else {
            val ret = f(row)
            ret
          }
      }
      .zipWithIndex
      .foldLeft(0) {
        case (acc, (b, i)) =>
          acc | (b << (30 - i * 2))
      }

    val constr = conditions.map(_._2.tree).mkString("\n")
    val pvstr = toBinaryStringWithLeadingZeros(pv).take(conditions.length*2)
    pv
  }

}
