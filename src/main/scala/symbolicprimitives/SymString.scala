package symbolicprimitives

import provenance.data.{DualRBProvenance, Provenance}

import scala.reflect.runtime.universe._

/**
  * Created by malig on 4/29/19.
  */
case class SymString(override val value: String, p: Provenance) extends SymAny(value, p) {

  /**
    * Unsupported Operations
    */

   def length: SymInt = {
     SymInt(value.length, getProvenance())
  }
  private def addColProv(p: Provenance, col: Int): Provenance = {
    p match {
      case _ : DualRBProvenance =>
        if(col > DualRBProvenance.MAX_COL_VAL) {
          throw new UnsupportedOperationException(s"Number of columns exceeded max allowed value of ${DualRBProvenance.MAX_COL_VAL} during call to split")
        }
        p // TODO: If we stick with instrumenting split for col prov, then the column prov will be added here using masking
      case _ => p
    }
  }

  def split(separator: Char): Array[SymString] = {
    var col = -1
     value
      .split(separator)
      .map(s => {
        col+=1
        SymString(
          s, addColProv(getProvenance(), col))
      })
  }
  def split(regex: String): Array[SymString] = {
    split(regex, 0)
  }

  def split(regex: String, limit: Int): Array[SymString] = {
    value
      .split(regex, limit)
      .map(s =>
        SymString(
          s, getProvenance()))
  }
   def split(separator: Array[Char]): Array[SymString] = {

    value
      .split(separator)
      .map(s =>
         SymString(
          s, getProvenance()
        ))
  }

  def substring(arg0: SymInt): SymString = {
      SymString(value.substring(arg0.value), newProvenance(arg0.getProvenance()))
  }

  def substring(arg0: Int, arg1: SymInt): SymString = {
    SymString(value.substring(arg0, arg1.value), newProvenance(arg1.getProvenance()))
  }
  def substring(arg0: SymInt, arg1: SymInt): SymString = {
    SymString(value.substring(arg0.value, arg1.value), newProvenance(arg0.getProvenance(), arg1.getProvenance()))
  }

  def lastIndexOf(elem: Char): SymInt = {
    SymInt(value.lastIndexOf(elem), getProvenance())
  }
  
  def trim(): SymString = {
    SymString(value.trim, getProvenance())
  }

   def toInt: SymInt ={
    SymInt( value.toInt, getProvenance())
  }

   def toFloat: SymFloat =
     SymFloat(value.toFloat , getProvenance())

   def toDouble: SymDouble ={
    SymDouble(value.toDouble , getProvenance())
  }

  // TODO: add configuration to track equality checks, e.g. if used as a key in a map.
  def equals(obj: SymString): Boolean = value.equals(obj.value)
  def eq(obj: SymString): Boolean = value.eq(obj.value)

  def +(x: SymString): SymString = {
    SymString(value + x.value, getProvenance().merge(x.getProvenance()))
  }
  def +(x: String): SymString = {
    SymString(value + x, getProvenance())
  }

}

object SymString {
  implicit def lift = Liftable[SymString] { si =>
    q"(_root_.symbolicprimitives.SymString(${si.value}, ${si.p}))"
  }
}