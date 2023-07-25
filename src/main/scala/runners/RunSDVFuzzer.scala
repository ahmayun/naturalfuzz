package runners

import spray.json._
import DefaultJsonProtocol._


import java.net.{InetAddress, Socket}
import java.io.{BufferedReader, InputStreamReader, PrintWriter}


object RunSDVFuzzer {
  val host = "localhost"
  val port = 24060

  def mapToJsonString(map: Map[String, Any]): String = {
    def valueToJson(value: Any): JsValue = value match {
      case v: String => JsString(v)
      case v: Int => JsNumber(v)
      case v: Double => JsNumber(v)
      case v: Boolean => JsBoolean(v)
      case v: Map[String, Any] => mapToJson(v)
      case v: Seq[_] => seqToJson(v)
      case v: Array[_] => seqToJson(v)
      case _ => JsNull
    }

    def seqToJson(seq: Seq[Any]): JsValue = {
      JsArray(seq.map(valueToJson).toVector)
    }

    def mapToJson(map: Map[String, Any]): JsObject = {
      JsObject(map.mapValues(valueToJson))
    }

    mapToJson(map).compactPrint
  }

  def sendMessage(message: String): String = {
    val socket = new Socket(InetAddress.getByName(host), port)
    val writer = new PrintWriter(socket.getOutputStream, true)
    val reader = new BufferedReader(new InputStreamReader(socket.getInputStream))

    writer.println(message)
    writer.flush()

    val response = new StringBuilder
    var line: String = null

    while ({ line = reader.readLine(); line } != null) {
      response.append(line)
    }

    reader.close()
    writer.close()
    socket.close()

    response.toString
  }



  def initializeModels(datasetPaths: List[String]): String = {

    val message = Map(
      "command" -> "initialize_models",
      "datasets" -> datasetPaths
    )
    sendMessage(mapToJsonString(message))
  }

  def generateData(numRows: Map[String, Int]): String = {
    val message = Map(
      "command" -> "generate_data",
      "num_rows" -> numRows
    )
    sendMessage(mapToJsonString(message))
  }

  def main(args: Array[String]): Unit = {
    // Initialize models
    val datasetPaths = List("/path/to/dataset1.csv", "/path/to/dataset2.csv")
    val initModelsResponse = initializeModels(datasetPaths)
    println(initModelsResponse)

    // Generate data
    val numRows = Map("dataset1" -> 100, "dataset2" -> 200)
    val generateDataResponse = generateData(numRows)
    println(generateDataResponse)
  }
}
