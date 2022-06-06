package symbolicexecution

import fuzzer.Program

object SymbolicExecutor {

  def execute(program: Program): SymExResult = {
    new SymExResult(program)
  }
}
