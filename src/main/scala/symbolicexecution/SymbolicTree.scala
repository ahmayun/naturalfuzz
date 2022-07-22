package symbolicexecution

import abstraction.BaseRDD
import fuzzer.Schema
import provenance.data.{DummyProvenance, Provenance}
import runners.Config
import taintedprimitives.TaintedBase
import utils.{Query, RDDLocations}

import scala.collection.mutable.ListBuffer


abstract case class SymTreeNode(s: Any) {

  def getValue: Any = s

  override def toString: String = s.toString
}

class OperationNode(override val s: Any) extends SymTreeNode(s) {

}

class ConcreteValueNode(override val s: Any) extends SymTreeNode(s) {

}

class ProvValueNode(override val s: Any, prov: Provenance) extends SymTreeNode(s) {

  def getProv: ListBuffer[(Int, Int, Int)] = prov.convertToTuples

  def getCol: Int = {
    getProv match {
      case ListBuffer((_, col, _)) => col
      case _ => throw new Exception("Provenance is ambiguous")
    }
  }

  def getDS: Int = {
    getProv match {
      case ListBuffer((ds, _, _)) => ds
      case _ => throw new Exception("Provenance is ambiguous")
    }
  }

  override def toString: String = getProv.map{case (ds, col, row) => s"rdd[$ds,$col,$row]"}.mkString("|") + s"{$s}"

}

case class SymbolicTree(left: SymbolicTree, node: SymTreeNode, right: SymbolicTree) {

  def this() = {
    this(null, null, null)
  }

  def this(n: SymTreeNode) = {
    this(null, n, null)
  }

  def height: Int = {
    (left, right) match {
      case (null, null) => 0
      case (null, r) => 1 + r.height
      case (l, null) => 1 + l.height
      case _ => 1 + math.max(left.height, right.height)
    }
  }

  def isOp(op: String): Boolean = {
    node.s.equals(op)
  }

  def isAtomic: Boolean = {
    !(isOp("&&") || isOp("||")) && ((left, right) match {
      case (null, null) => true
      case (null, r) => r.isAtomic
      case (l, null) => l.isAtomic
      case _ => left.isAtomic && right.isAtomic
    })
  }

  def breakIntoAtomic: List[SymbolicTree] = {
    if (isAtomic)
      List(this)
    else
      left.breakIntoAtomic ++ right.breakIntoAtomic
  }

  def isMultiDatasetQuery: Boolean = {
    false // TODO: implement this check
  }

  def isMultiColumnQuery: Boolean = {
    false // TODO: implement this check
  }

  def eval(row: Array[String], ds: Int): Any = {
    if(height == 0) {
      return node match {
        case n: ConcreteValueNode => n.s
        case n: ProvValueNode if n.getDS == ds =>
          // TODO: Add a callback to
          row(n.getCol).toInt //TODO: Remove hardcoded type conversion. Lookup using schema?
        case _ => true
      }
    }

    val leval = left.eval(row, ds)
    val reval = right.eval(row, ds)

    if(leval.isInstanceOf[Boolean] || reval.isInstanceOf[Boolean]) {
      return true
    }

    (leval, reval, node.s.toString) match {
      case (l: Int, r: Int, "<") => l < r
      case (l: Int, r: Int, ">") => l > r
      case (l: Int, r: Int, "<=") => l <= r
      case (l: Int, r: Int, ">=") => l >= r
      case (l: Int, r: Int, "==") => l == r
      case (l: Int, r: Int, "+") => l + r
      case (l: Int, r: Int, "-") => l - r
      case (l: Int, r: Int, "/") => l / r
    }
  }

  def createFilterFn(ds: Int): Array[String] => Boolean = {
    node match {
      case _: OperationNode => row => eval(row, ds).asInstanceOf[Boolean]
      case _ => throw new Exception("toQuery called on malformed tree")
    }
  }

  def getProv: ListBuffer[(Int, Int, Int)] = {

    (node match {
      case n: ProvValueNode => n.getProv
      case _ => ListBuffer()
    }) ++
      (if(left != null) left.getProv else ListBuffer()) ++
      (if(right != null) right.getProv else ListBuffer())
  }

  def toQuery: Query = {
    if(!isAtomic)
      throw new Exception("Attempted to convert non-atomic expression to query")
    else if(isMultiDatasetQuery)
      throw new Exception("Multi-Dataset queries not yet supported")

    def fq(rdds: Array[BaseRDD[String]]): Array[BaseRDD[String]] = {
      val filterFns = rdds.zipWithIndex.map{case (_, i) => this.createFilterFn(i)}
      val filterdrdds = rdds.zipWithIndex.map{case (ds, i) => ds.filter(row => filterFns(i)(row.split(',')))}
      println(s"filtered by $this")
      filterdrdds.foreach{
        println
      }
      println("filtered end")
      filterdrdds
    }

    new Query(fq, new RDDLocations(getProv.toArray), this)
  }

  def isEmpty: Boolean = this match {
    case SymbolicTree(null, null, null) => true
    case _ => false
  }

  override def toString: String = {
    this match {
      case SymbolicTree(null, null, null) => ""
      case SymbolicTree(null, node, null) => node.toString
      case _ => s"(${left.toString} $node ${right.toString})"
    }
  }
}

