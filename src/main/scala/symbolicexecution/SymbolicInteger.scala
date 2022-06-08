package symbolicexecution

class SymbolicInteger(val concrete: Option[Int]) extends SymbolicElement {

  def this() = {
    this(None)
  }

  def this(i: Int) = {
    this(Some(i))
  }
}
