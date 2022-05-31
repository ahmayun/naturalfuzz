package monitoring

import fuzzer.ProvInfo
import org.apache.spark.SparkContext
import org.roaringbitmap.RoaringBitmap
import provenance.data.DualRBProvenance
import provenance.rdd.{PairProvenanceDefaultRDD, PairProvenanceRDD}
import runners.Config
import symbolicprimitives.SymBase

import scala.collection.mutable.HashMap
import scala.reflect.ClassTag

object Monitors {

  val mapPredicateProvenance = new HashMap[Int, (RoaringBitmap, RoaringBitmap)]()
  val mapJoinProvenance = new HashMap[Int, (RoaringBitmap, RoaringBitmap)]
  val reduce_map = new HashMap[Int, RoaringBitmap]
  val gbk_map = new HashMap[Int, RoaringBitmap]
  val agg_samples = 100
  val max_samples = 5


  def monitorJoin[K<:SymBase:ClassTag,V1,V2](id: Int,
                                             d1: PairProvenanceDefaultRDD[K,V1],
                                             d2: PairProvenanceDefaultRDD[K,V2],
                                             sc: SparkContext = null): PairProvenanceRDD[K,(V1,V2)] = {
    println(s"joinWrapper $id d1")
    val d1j = d1.join(d2)
    val d2j = d2.join(d1)
    d1j.map(r => r._1).take(agg_samples).foreach (
      sample => {
        println(sample)
        val (old, _) = mapJoinProvenance.getOrElseUpdate(id, (new RoaringBitmap, new RoaringBitmap))
        old.or(sample.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
      }
    )

    println(s"joinWrapper $id d2")
    d2j.map(r => r._1).take(agg_samples).foreach (
      sample => {
        println(sample)
        val (_, old) = mapJoinProvenance.getOrElseUpdate(id, (new RoaringBitmap, new RoaringBitmap))
        old.or(sample.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
      }
    )
    d1j
  }

  def updateMap(id: Int, prov1: RoaringBitmap, prov2: RoaringBitmap): Unit = {
    val (p1, p2) = mapPredicateProvenance.getOrElseUpdate(id, (new RoaringBitmap, new RoaringBitmap))
    p1.or(prov1)
    p2.or(prov2)
  }

  // provWrapper(AST, (vars_thisbranch, vars_prevbranch), branch_id);
  def monitorPredicate(bool: Boolean, exp: reflect.runtime.universe.Tree, prov: (List[Any], List[Any]), id: Int): Boolean = {
    if (bool) {
      val this_branch_prov = new RoaringBitmap()
      val prev_branch_prov = new RoaringBitmap()

      prov._1.foreach {
        case v: SymBase => this_branch_prov.or(v.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
        case _ =>
      }

      prov._2.foreach {
        case v: SymBase => prev_branch_prov.or(v.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
        case _ =>
      }

      updateMap(id, this_branch_prov, prev_branch_prov)
    }
    bool
  }

  def monitorPredicate(bool: Boolean, prov: (List[Any], List[Any]), id: Int): Boolean = {
    if (bool) {
      val this_branch_prov = new RoaringBitmap()
      val prev_branch_prov = new RoaringBitmap()

      prov._1.foreach {
        case v: SymBase => this_branch_prov.or(v.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
        case _ =>
      }

      prov._2.foreach {
        case v: SymBase => prev_branch_prov.or(v.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
        case _ =>
      }

      updateMap(id, this_branch_prov, prev_branch_prov)
    }
    bool
  }

  def monitorGroupByKey[K<:SymBase:ClassTag,V:ClassTag](id: Int, dataset: PairProvenanceDefaultRDD[K,V]): PairProvenanceDefaultRDD[K, Iterable[V]] = {
    val prov = dataset.take(agg_samples)(0)._1.getProvenance().asInstanceOf[DualRBProvenance].bitmap
    gbk_map.update(id, prov)
    dataset.groupByKey()
  }

  def monitorReduceByKey[K<:SymBase:ClassTag,V](id: Int, dataset: PairProvenanceDefaultRDD[K,V], func: (V, V) => V): PairProvenanceRDD[K, V] = {
    val reduced = dataset.reduceByKey(func)

    reduced.map(r => r._1).take(agg_samples).foreach (
      sample => {
        val old = reduce_map.getOrElseUpdate(id, new RoaringBitmap)
        old.or(sample.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
      }
    )
    reduced
  }

  // called at the end of main function
  def finalizeProvenance(): ProvInfo = {
    //    val min_data: Array[Seq[String]] = Array.fill(2)(Seq())
    //    val join_relations = postProcessDependencies(this.mapJoinProvenance.map {
    //      case (k, (originalOrder, revOrder)) =>
    //        val this_tups = Utils.retrieveColumnsFromBitmap(originalOrder) // [(ds1, col1), (ds1, col2), (ds2, col0), (ds2, col2)]
    //        val rev_tups = Utils.retrieveColumnsFromBitmap(revOrder)
    //        val this_ds_cols = this_tups.groupBy(_._1).map { case (ds, list) => (ds, list.map(_._2)) }
    //        val rev_ds_cols = rev_tups.groupBy(_._1).map { case (ds, list) => (ds, list.map(_._2)) }
    //        //        println("this ds")
    //        //        this_ds_cols.foreach(println)
    //        //        println("rev ds")
    //        //        rev_ds_cols.foreach(println)
    //        val (this_ds, rev_ds) = (this_ds_cols.keySet.toVector(0), rev_ds_cols.keySet.toVector(0))
    //        val this_rownums = Utils.retrieveRowNumbers(new DualRBProvenance(originalOrder), this_ds_cols.keySet.toVector(0)).collect().take(this.max_samples)
    //        val rev_rownums = Utils.retrieveRowNumbers(new DualRBProvenance(revOrder), rev_ds_cols.keySet.toVector(0)).collect().take(this.max_samples)
    //        val this_rows = Utils.retrieveProvenance(new DualRBProvenance(originalOrder), this_ds).collect().take(this.agg_samples)
    //        val rev_rows = Utils.retrieveProvenance(new DualRBProvenance(revOrder), rev_ds).collect().take(this.agg_samples)
    //        min_data(this_ds) ++= this_rows.toSeq
    //        min_data(rev_ds) ++= rev_rows.toSeq
    //        min_data.foreach(s => s.foreach(println))
    //        this_ds_cols.map { case (ds, cols) => (ds, cols.toArray, this_rownums) }.toArray ++ rev_ds_cols.map { case (ds, cols) => (ds, cols.toArray, rev_rownums) }
    //    }.toArray)
    //
    //    this.mapPredicateProvenance.foreach {
    //      prov =>
    //        val (k, (v1, v2)) = prov
    //        val this_tups = Utils.retrieveColumnsFromBitmap(v1) // [(ds1, col1), (ds1, col2), (ds2, col0), (ds2, col2)]
    //        val rev_tups = Utils.retrieveColumnsFromBitmap(v2)
    //        val this_ds_cols = this_tups.groupBy(_._1).map { case (ds, list) => (ds, list.map(_._2)) }
    //        val prev_ds_cols = rev_tups.groupBy(_._1).map { case (ds, list) => (ds, list.map(_._2)) }
    //
    //        try {
    //          val this_ds = this_ds_cols.keySet.toVector(0)
    //          val this_rows = Utils.retrieveProvenance(new DualRBProvenance(v1), this_ds).collect().take(max_samples)
    //          min_data(this_ds) ++= this_rows.toSeq
    //        } catch {
    //          case _ =>
    //        }
    //
    //        try {
    //          val prev_ds = prev_ds_cols.keySet.toVector(0)
    //          val prev_rows = Utils.retrieveProvenance(new DualRBProvenance(v2), prev_ds).collect().take(max_samples)
    //          min_data(prev_ds) ++= prev_rows.toSeq
    //        } catch {
    //          case _ =>
    //        }
    //
    //    }
    //

    new ProvInfo(
      Config.benchmarkName match {
        case "WebpageSegmentation" if Config.seedType.equals("weak") =>
          /*
            RAW:
            (1,0,0)
            -------
            (0,0,0)<=>(0,5,0)<=>(0,6,0)<=>(1,0,0)<=>(1,5,0)<=>(1,6,0)
            ---------------------------------------------------------
            (1,0,0)<=>(1,0,0)
            -----------------
            SIMPLIFIED:
            (0,0,0)<=>(0,5,0)<=>(0,6,0)<=>(1,0,0)<=>(1,5,0)<=>(1,6,0)
            ---------------------------------------------------------
           */
          Array(
            Array((1,0,0)),
            Array(0,1).flatMap(d => Array(0,5,6).map(c => (d, c, 0))),
            Array((1, 0, 0), (1, 0, 0))
          )
        case _ => Array()
      }
    ).simplify()
  }
}
