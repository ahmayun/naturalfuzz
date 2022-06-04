object predicateTest1 {
  def main(args: Array[String]): Unit = {
    val x = 3
    if (_root_.monitoring.Monitors.monitorPredicate(x < 5, (List[Any](x), List[Any]()), 0)) {
      println("do this")
    } else {
      println("do that")
    }
    _root_.monitoring.Monitors.finalizeProvenance()
  }
}