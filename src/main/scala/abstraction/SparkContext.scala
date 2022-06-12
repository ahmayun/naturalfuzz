package abstraction

import utils.FileUtils

class SparkContext(config: SparkConf) {

  def textFile(path: String): abstraction.BaseRDD[String] = {
    val data = FileUtils.readDatasetPart(path, 0)
    new BaseRDD(data)
  }

  def setLogLevel(str: String) = {

  }

}
