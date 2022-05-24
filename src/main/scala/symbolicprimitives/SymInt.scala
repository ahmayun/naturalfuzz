package symbolicprimitives

/**
  * Created by malig on 4/25/19.
  */

import provenance.data.{DummyProvenance, Provenance}

import scala.reflect.runtime.universe._

case class SymInt(override val value: Int, p : Provenance) extends SymAny(value, p) {

  def this(value: Int) = {
    this(value, DummyProvenance.create())
  }
  
  /**
    * Overloading operators from here onwards
    */
  def +(x: Int): SymInt = {
    val d = value + x
    SymInt(d, getProvenance())
  }

  def -(x: Int): SymInt = {
    val d = value - x
    SymInt(d, getProvenance())
  }

  def *(x: Int): SymInt = {
    val d = value * x
    SymInt(d, getProvenance())
  }

  def *(x: Float): SymFloat = {
    val d = value * x
    SymFloat(d, getProvenance())
  }


  def /(x: Int): SymDouble= {
    val d = value / x
    SymDouble(d, getProvenance() )
  }

  def /(x: Long): SymDouble= {
    val d = value / x
    SymDouble(d, getProvenance())
  }

  def +(x: SymInt): SymInt = {
    SymInt(value + x.value, newProvenance(x.getProvenance()))
  }

  def -(x: SymInt): SymInt = {
    SymInt(value - x.value, newProvenance(x.getProvenance()))
  }

  def *(x: SymInt): SymInt = {
    SymInt(value * x.value, newProvenance(x.getProvenance()))
  }

  def /(x: SymInt): SymInt = {
    SymInt(value / x.value, newProvenance(x.getProvenance()))
  }

  def %(x: Int): SymInt = {
    SymInt(value % x, p)
  }
  
  // Implementing on a need-to-use basis
  def toInt: SymInt = this
  def toDouble: SymDouble = SymDouble(value.toDouble, getProvenance())
  
  /**
    * Operators not supported yet
    */

  def ==(x: Int): Boolean = value == x

  def toByte: Byte = value.toByte

  def toShort: Short = value.toShort

  def toChar: Char = value.toChar

  

  def toLong: Long = value.toLong

  def toFloat: SymFloat = new SymFloat(value.toFloat, p)

  //def toDouble: Double = value.toDouble

  def unary_~ : Int = value.unary_~

  def unary_+ : Int = value.unary_+

  def unary_- : Int = value.unary_-

  def +(x: String): String = value + x

  def <<(x: Int): Int = value << x

  def <<(x: Long): Int = value << x

  def >>>(x: Int): Int = value >>> x

  def >>>(x: Long): Int = value >>> x

  def >>(x: Int): Int = value >> x

  def >>(x: Long): Int = value >> x

  def ==(x: Byte): Boolean = value == x

  def ==(x: Short): Boolean = value == x

  def ==(x: Char): Boolean = value == x

  def ==(x: Long): Boolean = value == x

  def ==(x: Float): Boolean = value == x

  def ==(x: Double): Boolean = value == x

  def !=(x: Byte): Boolean = value != x

  def !=(x: Short): Boolean = value != x

  def !=(x: Char): Boolean = value != x

  def !=(x: Int): Boolean = value != x

  def !=(x: Long): Boolean = value != x

  def !=(x: Float): Boolean = value != x

  def !=(x: Double): Boolean = value != x

  def <(x: Byte): Boolean = value < x

  def <(x: Short): Boolean = value < x

  def <(x: Char): Boolean = value < x

  def <(x: Int): Boolean = value < x

  def <(x: Long): Boolean = value < x

  def <(x: Float): Boolean = value < x

  def <(x: Double): Boolean = value < x

  def <=(x: Byte): Boolean = value <= x

  def <=(x: Short): Boolean = value <= x

  def <=(x: Char): Boolean = value <= x

  def <=(x: Int): Boolean = value <= x

  def <=(x: Long): Boolean = value <= x

  def <=(x: Float): Boolean = value <= x

  def <=(x: Double): Boolean = value <= x

  def >(x: Byte): Boolean = value > x

  def >(x: Short): Boolean = value > x

  def >(x: Char): Boolean = value > x

  def >(x: Int): Boolean = value > x

  def >(x: Long): Boolean = value > x

  def >(x: Float): Boolean = value > x

  def >(x: Double): Boolean = value > x

  def >=(x: Byte): Boolean = value >= x

  def >=(x: Short): Boolean = value >= x

  def >=(x: Char): Boolean = value >= x

  def >=(x: Int): Boolean = value >= x

  def >=(x: Long): Boolean = value >= x

  def >=(x: Float): Boolean = value >= x

  def >=(x: Double): Boolean = value >= x

  def |(x: Byte): Int = value | x

  def |(x: Short): Int = value | x

  def |(x: Char): Int = value | x

  def |(x: Int): Int = value | x

  def |(x: Long): Long = value | x

  def &(x: Byte): Int = value & x

  def &(x: Short): Int = value & x

  def &(x: Char): Int = value & x

  def &(x: Int): Int = value & x

  def &(x: Long): Long = value & x

  def ^(x: Byte): Int = value ^ x

  def ^(x: Short): Int = value ^ x

  def ^(x: Char): Int = value ^ x

  def ^(x: Int): Int = value ^ x

  def ^(x: Long): Long = value ^ x

  def +(x: Byte): Int = value + x

  def +(x: Short): Int = value + x

  def +(x: Char): Int = value + x

  def +(x: Long): Long = value + x

  def +(x: Float): Float = value + x

  def +(x: Double): Double = value + x

  def -(x: Byte): Int = value - x

  def -(x: Short): Int = value - x

  def -(x: Char): Int = value - x

  def -(x: Long): Long = value - x

  def -(x: Float): Float = value - x

  def -(x: Double): Double = value - x

  def *(x: Byte): Int = value * x

  def *(x: Short): Int = value * x

  def *(x: Char): Int = value * x

  def *(x: Long): Long = value * x

  def *(x: Double): Double = value * x

  def /(x: Byte): Int = value / x

  def /(x: Short): Int = value / x

  def /(x: Char): Int = value / x

  def /(x: Float): Float = value / x

  def /(x: Double): Double = value / x

  def %(x: Byte): Int = value % x

  def %(x: Short): Int = value % x

  def %(x: Char): Int = value % x

  def %(x: Long): Long = value % x

  def %(x: Float): Float = value % x

  def %(x: Double): Double = value % x

}

object SymInt {
  implicit def lift = Liftable[SymInt] { si =>
    q"(_root_.symbolicprimitives.SymInt(${si.value}, ${si.p}))"
  }

  implicit def ordering: Ordering[SymInt] = Ordering.by(_.value)
}
