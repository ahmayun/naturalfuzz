package symbolicprimitives

import provenance.data.{DummyProvenance, Provenance}

import scala.reflect.runtime.universe._


/**
  * Created by malig on 4/25/19.
  */
case class SymDouble(i: Double, p: Provenance) extends SymAny(i, p) {
  def this(value: Double) = {
    this(value, DummyProvenance.create())
  }
  /**
    * Overloading operators from here onwards
    */

  def +(x: Double): SymDouble = {
    SymDouble(value + x, getProvenance())
  }

  def -(x: Double): SymDouble = {
    SymDouble(value - x, getProvenance())
  }

  def *(x: Double): SymDouble = {
    SymDouble(value * x, getProvenance())

  }

  def *(x: Float): SymDouble = {
    SymDouble(value * x, getProvenance())
  }

  def /(x: Double): SymDouble = {
    SymDouble(value / x, getProvenance())
  }

  def +(x: SymDouble): SymDouble = {
    SymDouble(value + x.value, newProvenance(x.getProvenance()))
  }

  def -(x: SymDouble): SymDouble = {
    SymDouble(value - x.value, newProvenance(x.getProvenance()))
  }

  def *(x: SymDouble): SymDouble = {
    SymDouble(value * x.value, newProvenance(x.getProvenance()))
  }

  def /(x: SymDouble): SymDouble = {
    SymDouble(value / x.value, newProvenance(x.getProvenance()))
  }

  // TODO: Following are control flow provenance that, in my opinion, should be configurable. [Gulzar]
  // JT: agreed - for now, I've disabled since they don't return a new data type.
  def <(x: SymDouble): Boolean = {
    // mergeProvenance(x.getProvenance()) // see above note on configurable
    return value < x.value
  }

  def <(x: Double): Boolean = {
    return value < x
  }
  
  def >(x: SymDouble): Boolean = {
    // mergeProvenance(x.getProvenance()) // see above note on configurable
    return value > x.value
  }
  
  def >(x: Double): Boolean = {
    return value > x
  }

  def >=(x: SymDouble): Boolean = {
    // mergeProvenance(x.getProvenance()) // see above note on configurable
    value >= x.value
  }
  def <=(x: SymDouble): Boolean = {
    // mergeProvenance(x.getProvenance()) // see above note on configurable
    value <= x.value
  }

  def ==(x: Int): Boolean = {
    value == x
  }
  def >(x: Int): Boolean = {
    value > x
  }

  /**
    * Not Supported Symbolically yet
    **/
  //
  //  def toByte: Byte = value.toByte
  //
  //  def toShort: Short = value.toShort
  //
  //  def toChar: Char = value.toChar
  //
  //  def toInt: Int = value.toInt
  //
  //  def toLong: Long = value.toLong
  //
  //  def toFloat: Float = value.toFloat
  //
  //  def toDouble: Double = value.toDouble
  //
  //  def unary_~ : Int = value.unary_~
  //
  //  def unary_+ : Int = value.unary_+
  //
  //  def unary_- : Int = value.unary_-
  //
  //  def +(x: String): String = value + x
  //
  //  def <<(x: Int): Int = value << x
  //
  //  def <<(x: Long): Int = value << x
  //
  //  def >>>(x: Int): Int = value >>> x
  //
  //  def >>>(x: Long): Int = value >>> x
  //
  //  def >>(x: Int): Int = value >> x
  //
  //  def >>(x: Long): Int = value >> x
  //
  //  def ==(x: Byte): Boolean = value == x
  //
  //  def ==(x: Short): Boolean = value == x
  //
  //  def ==(x: Char): Boolean = value == x
  //
  //  def ==(x: Long): Boolean = value == x
  //
  //  def ==(x: Float): Boolean = value == x
  //
  //  def ==(x: Double): Boolean = value == x
  //
  //  def !=(x: Byte): Boolean = value != x
  //
  //  def !=(x: Short): Boolean = value != x
  //
  //  def !=(x: Char): Boolean = value != x
  //
  //  def !=(x: Int): Boolean = value != x
  //
  //  def !=(x: Long): Boolean = value != x
  //
  //  def !=(x: Float): Boolean = value != x
  //
  //  def !=(x: Double): Boolean = value != x
  //
  //  def <(x: Byte): Boolean = value < x
  //
  //  def <(x: Short): Boolean = value < x
  //
  //  def <(x: Char): Boolean = value < x
  //
  //  def <(x: Int): Boolean = value < x
  //
  //  def <(x: Long): Boolean = value < x
  //
  //  def <(x: Float): Boolean = value < x
  //
  //
  //
  //  def <=(x: Byte): Boolean = value <= x
  //
  //  def <=(x: Short): Boolean = value <= x
  //
  //  def <=(x: Char): Boolean = value <= x
  //
  //  def <=(x: Int): Boolean = value <= x
  //
  //  def <=(x: Long): Boolean = value <= x
  //
  //  def <=(x: Float): Boolean = value <= x
  //
  //  def <=(x: Double): Boolean = value <= x
  //
  //  def >(x: Byte): Boolean = value > x
  //
  //  def >(x: Short): Boolean = value > x
  //
  //  def >(x: Char): Boolean = value > x
  //
  //  def >(x: Int): Boolean = value > x
  //
  //  def >(x: Long): Boolean = value > x
  //
  //  def >(x: Float): Boolean = value > x
  //
  //  def >(x: Double): Boolean = value > x
  //
  //  def >=(x: Byte): Boolean = value >= x
  //
  //  def >=(x: Short): Boolean = value >= x
  //
  //  def >=(x: Char): Boolean = value >= x
  //
  //  def >=(x: Int): Boolean = value >= x
  //
  //  def >=(x: Long): Boolean = value >= x
  //
  //  def >=(x: Float): Boolean = value >= x
  //
  //  def >=(x: Double): Boolean = value >= x
  //
  //  def |(x: Byte): Int = value | x
  //
  //  def |(x: Short): Int = value | x
  //
  //  def |(x: Char): Int = value | x
  //
  //  def |(x: Int): Int = value | x
  //
  //  def |(x: Long): Long = value | x
  //
  //  def &(x: Byte): Int = value & x
  //
  //  def &(x: Short): Int = value & x
  //
  //  def &(x: Char): Int = value & x
  //
  //  def &(x: Int): Int = value & x
  //
  //  def &(x: Long): Long = value & x
  //
  //  def ^(x: Byte): Int = value ^ x
  //
  //  def ^(x: Short): Int = value ^ x
  //
  //  def ^(x: Char): Int = value ^ x
  //
  //  def ^(x: Int): Int = value ^ x
  //
  //  def ^(x: Long): Long = value ^ x
  //
  //  def +(x: Byte): Int = value + x
  //
  //  def +(x: Short): Int = value + x
  //
  //  def +(x: Char): Int = value + x
  //
  //  def +(x: Long): Long = value + x
  //
  //  def +(x: Float): Float = value + x
  //
  //  def +(x: Double): Double = value + x
  //
  //  def -(x: Byte): Int = value - x
  //
  //  def -(x: Short): Int = value - x
  //
  //  def -(x: Char): Int = value - x
  //
  //  def -(x: Long): Long = value - x
  //
  //  def -(x: Float): Float = value - x
  //
  //  def -(x: Double): Double = value - x
  //
  //  def *(x: Byte): Int = value * x
  //
  //  def *(x: Short): Int = value * x
  //
  //  def *(x: Char): Int = value * x
  //
  //  def *(x: Long): Long = value * x
  //
  //  def *(x: Float): Float = value * x
  //
  //  def *(x: Double): Double = value * x
  //
  //  def /(x: Byte): Int = value / x
  //
  //  def /(x: Short): Int = value / x
  //
  //  def /(x: Char): Int = value / x
  //
  //  def /(x: Long): Long = value / x
  //
  //  def /(x: Float): Float = value / x
  //
  //  def /(x: Double): Double = value / x
  //
  //  def %(x: Byte): Int = value % x
  //
  //  def %(x: Short): Int = value % x
  //
  //  def %(x: Char): Int = value % x
  //
  //  def %(x: Int): Int = value % x
  //
  //  def %(x: Long): Long = value % x
  //
  //  def %(x: Float): Float = value % x
  //
  //  def %(x: Double): Double = value % x
}

object SymDouble {
  implicit def ordering: Ordering[SymDouble] = Ordering.by(_.value)

  implicit def lift = Liftable[SymDouble] { si =>
    q"(_root_.symbolicprimitives.SymDouble(${si.value}, ${si.p}))"
  }
}
