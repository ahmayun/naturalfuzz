package sparkwrapper

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import provenance.data.Provenance
import provenance.rdd.{FlatProvenanceDefaultRDD, ProvenanceRDD}
//import sparkwrapper.SparkContextWithDP.{datasets, incrementOnce}
import symbolicprimitives.{SymString, Utils}

import scala.reflect.ClassTag

/**
  * Created by malig on 12/3/19.
  */
class SparkContextWithDP(sc: SparkContext) {

  var datasets = 0

  def textFile(filepath: String): RDD[String] ={
    sc.textFile(filepath)
  }

  def setLogLevel(s: String): Unit = {
    sc.setLogLevel(s)
  }
  
  private def textFileProvenanceCreator[T: ClassTag](filepath: String,
                                           followup: (String, Provenance) => T): RDD[T] = {
    val rdd = sc.textFile(filepath)
    // Ugly note: we need to have a handle to the actual provenance factory in case we've
    // adjusted this in our application (since the change needs to be propagated to all
    // machines in the cluster). Provenance.create() by itself will lazily evaluate
    // provenanceFactory, which won't be updated on worker nodes if changed in-application.
    val provCreatorFn = Provenance.createFn()
    Utils.setInputZip(rdd.zipWithUniqueId())
         .map { record =>
                  val prov = provCreatorFn(Seq(record._2))
                  followup(record._1, prov)
              }
  }
  
  /** Text file with Symbolic strings (no provenance RDD) */
  def textFileUDFProv(filepath: String): RDD[SymString] = {
    textFileProvenanceCreator(filepath, SymString.apply)
  }

  /** Text file with symbolic strings and provenance RDDs. */
  def textFileSymbolic(filepath: String): ProvenanceRDD[SymString] = {
    if(!Utils.getUDFAwareEnabledValue(None)) {
      // TODO jteoh: we might be able to remove this warning if we determine at collect-time that
      //  the output we are collecting is a symbolic type?
      println("WARNING: Did you mean to enable UDF Aware provenance since you are using " +
                "textFileSymbolic?")
    }
    val baseRDD = textFileProvenanceCreator(filepath, (str, prov) => (SymString(str, prov), prov))
    new FlatProvenanceDefaultRDD(baseRDD)
  }
  
  /** Text file with provenance RDDs (but no symbolic strings) */
  def textFileProv(filepath: String): ProvenanceRDD[String] = {
    // have to define this because predef identity was detecting as Nothing => Nothing
    val identity = (s: String, p: Provenance) => (s, p)
    val baseRDD = textFileProvenanceCreator(filepath, identity)
    new FlatProvenanceDefaultRDD[String](baseRDD)
  }

  private def textFileProvenanceCreator[T: ClassTag](filepath: String,
                                                     followup: (Array[SymString], Provenance) => T,
                                                     createCol: String => Array[String]
                                                    ): RDD[T] = {
    val rdd = sc.textFile(filepath)
    val temp = datasets
    val ret = Utils.setInputZip(rdd.zipWithUniqueId().map{
      record => {
//        println(s"rdd-id ${rdd.id}")
        Utils.attachProv(record, followup, createCol, temp)
      }
    })
    datasets += 1
    ret
  }

  def textFileProv(filepath: String, createCol: String => Array[String]): ProvenanceRDD[Array[SymString]] = {
    // have to define this because predef identity was detecting as Nothing => Nothing
    val identity = (s: Array[SymString], p: Provenance) => (s, p)
    val baseRDD = textFileProvenanceCreator(filepath, identity, createCol)
    Utils.setUDFAwareDefaultValue(true)
    new FlatProvenanceDefaultRDD[Array[SymString]](baseRDD)
  }
}

object SparkContextWithDP {
//  var datasets = -1
//  val set = new mutable.HashSet[Int]()
//
//  def incrementOnce(id: Int) = {
//    if(!set.contains(id)){
//      set.add(id)
//      datasets += 1
//    }
//  }
}