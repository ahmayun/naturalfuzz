package refactor

object TestMonitorAttacher {

  def main(args: Array[String]): Unit = {
    val program =
      """
        |package examples.benchmarks.input_reduction_benchmarks
        |object AggOpWrapperTest {
        |
        |  def main(args: Array[String]): Unit = {
        |
        |    val joined = c.join(o)
        |    val grouped = c.groupByKey()
        |
        |  }
        |}
        |""".stripMargin

    val basepath = "src/main/scala/examples"
    val benchfolder = s"$basepath/benchmarks"
    val trackedfolder = s"$basepath/monitored"
    val file = "DeliveryFaults.scala"
    val readpath = s"$benchfolder/$file"
    val writepath = s"$trackedfolder/probe_$file"
    //        val tree = program.parse[Source].get
    val tree = MonitorAttacher.treeFromFile(readpath)
    val transformed = MonitorAttacher(tree)
    println("-" * 50)
    println(tree.structure)
    println("-" * 50)
    println(transformed)
    println("-" * 50)
    MonitorAttacher.writeTransformed(transformed.toString, writepath)
  }
}