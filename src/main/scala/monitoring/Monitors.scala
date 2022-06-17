package monitoring

import fuzzer.ProvInfo
import org.apache.spark.rdd.RDD
import org.roaringbitmap.RoaringBitmap
import provenance.data.{DualRBProvenance, Provenance}
import provenance.rdd.{PairProvenanceDefaultRDD, PairProvenanceRDD}
import runners.Config
import symbolicexecution.{SymbolicExpression, SymbolicTree}
import taintedprimitives.{TaintedBase, TaintedBoolean}
import taintedprimitives.SymImplicits._

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

object Monitors {


  val provInfo: ProvInfo = new ProvInfo()


  def monitorJoin[K<:TaintedBase:ClassTag,V1,V2](d1: PairProvenanceDefaultRDD[K,V1],
                                                 d2: PairProvenanceDefaultRDD[K,V2],
                                                 id: Int): PairProvenanceRDD[K,(V1,V2)] = {

    val joint = d1.join(d2.map{case (k, v) => (k, (k, v))})
    val count = joint.count()
    count match {
      case 0 =>
        val buffer1 = d1.sample(false, 0.5*Config.maxSamples/d1.count()).map {
          case (k1, _) =>
            k1.getProvenance()
        }.collect().to[ListBuffer]
        val buffer2 = d2.sample(false, 0.5*Config.maxSamples/d2.count()).map {
          case (k2, _) =>
            k2.getProvenance()
        }.collect().to[ListBuffer]
        buffer1
          .zip(buffer2)
          .foreach { case (p1, p2) => this.provInfo.update(id, ListBuffer(p1, p2)) }
      case _ =>
        joint.sample(false, Config.maxSamples/count).foreach {
        case (k1, (_, (k2, _))) =>
          this.provInfo.update(id, ListBuffer(k1.getProvenance(), k2.getProvenance()))
      }
    }

    joint.map{
      case (k1, (v1, (k2, v2))) =>
        k1.setProvenance(k1.getProvenance().merge(k2.getProvenance()))
        (k1, (v1, v2))
    }
  }

//  def monitorPredicate(bool: Boolean, prov: (List[Any], List[Any]), id: Int): Boolean = {
//    if (bool) {
//      prov._1.foreach {
//        case v: TaintedBase => // this.provInfo.update(id, ListBuffer(v.getProvenance()))
//        case _ =>
//      }
//    }
//    bool
//  }

  def monitorPredicate(bool: TaintedBoolean, prov: (List[Any], List[Any]), id: Int, currentPathConstraint: SymbolicExpression = SymbolicExpression(new SymbolicTree())): Boolean = {
    if (bool) {
      prov._1.foreach {
        case v: TaintedBase => // this.provInfo.update(id, ListBuffer(v.getProvenance()))
        case _ =>
      }
    }

    val pc = if(!currentPathConstraint.isEmpty)
      currentPathConstraint.and(bool.symbolicExpression)
    else
      bool.symbolicExpression

    println(s"PC for branch $id: $pc")
    bool
  }

  def monitorGroupByKey[K<:TaintedBase:ClassTag,V:ClassTag](dataset: PairProvenanceDefaultRDD[K,V], id: Int): PairProvenanceDefaultRDD[K, Iterable[V]] = {
    dataset
      .sample(false, Config.maxSamples/dataset.count())
      .foreach {
        case (k, _) => this.provInfo.update(id, ListBuffer(k.getProvenance()))
      }
    dataset.groupByKey()
  }

  def monitorReduceByKey[K<:TaintedBase:ClassTag,V](dataset: PairProvenanceDefaultRDD[K,V], func: (V, V) => V, id: Int): PairProvenanceRDD[K, V] = {
    dataset
      .sample(false, Config.maxSamples/dataset.count())
      .foreach {
        case (k, _) => this.provInfo.update(id, ListBuffer(k.getProvenance()))
      }
    dataset.reduceByKey(func)
  }

  def monitorFilter[T](rdd: RDD[T], f: T => Boolean): RDD[T] = {
    rdd
  }

  // called at the end of main function
  def finalizeProvenance(): ProvInfo = {
    provInfo.simplify()
  }
}