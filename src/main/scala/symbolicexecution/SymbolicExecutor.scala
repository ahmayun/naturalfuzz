package symbolicexecution

import fuzzer.SymbolicProgram

import scala.collection.mutable.ListBuffer

object SymbolicExecutor {

  def execute(program: SymbolicProgram): SymExResult = {
    program.main(program.args)
  }
}
