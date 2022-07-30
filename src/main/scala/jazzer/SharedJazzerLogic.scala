package jazzer

import com.code_intelligence.jazzer.api.FuzzedDataProvider

import java.io.File

object SharedJazzerLogic {
  def createMeasurementDir(path: String): Unit = {
    val success = new File(path).mkdirs()
    if(success)
      println(s"successfully created $path")
    else
      println(s"failed to create $path")
  }


  def createMutatedDatasets(data: FuzzedDataProvider, datasets: Array[String]): Array[String] = {
    println(s"createMutatedDatasets() - Bytes: ${data.remainingBytes()}")
    utils.FileUtils.writeToFile(Seq("Testing"), "/jazzer-output/test2")
    datasets
  }
}
