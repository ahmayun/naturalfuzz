package capture

object IOStreams {

  def _println(x: Any): Unit = {
    fuzzer.Global.stdout += s"${x.toString}\n"
    println(x)
  }

}
