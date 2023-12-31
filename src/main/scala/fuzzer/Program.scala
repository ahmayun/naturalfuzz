package fuzzer

class Program(val name: String,
              val classname: String,
              val classpath: String,
              val main: Array[String] => Unit,
              val args: Array[String]) {

}

// Can add this as an overloaded constructor because scala complains
class InstrumentedProgram(val name: String,
                          val classname: String,
                          val classpath: String,
                          val main: Array[String] => ProvInfo,
                          val args: Array[String]) {

}

