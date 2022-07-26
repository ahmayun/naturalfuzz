package jazzer

import com.code_intelligence.jazzer.api.FuzzedDataProvider

object SharedJazzerLogic {

  def createMutatedDatasets(data: FuzzedDataProvider, datasets: Array[String]): Array[String] = {
    println(s"createMutatedDatasets() - Bytes: ${data.remainingBytes()}")
    utils.FileUtils.writeToFile(Seq("Testing"), "/jazzer-output/test2")
    datasets
  }
}
