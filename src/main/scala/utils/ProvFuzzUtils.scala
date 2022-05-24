package utils


import fuzzer.{InstrumentedProgram, ProvInfo, Schema}
import generators.GenSegmentationData.deleteDir

import java.io.{BufferedWriter, File, FileWriter}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future, blocking, duration}
import scala.util.Random

object ProvFuzzUtils {
  def RandomSampleDatasets(datasets: Array[String], out_dir: String, sample_size: Double): Array[String] = {
    val data = ReadDatasets(datasets)
    val spds = sample_size/datasets.length
    val sampled_data = data.map(d => SelectRandomRows(d, spds))
    WriteDatasets(sampled_data, out_dir)
  }

  def SelectRandomRows(data: Seq[String], per: Double): Seq[String] = {
    val num_samples = (data.length*per).toInt
    val idxs = (0 until num_samples).map(_ => Random.nextInt(data.length))
    data.zipWithIndex.filter{case (_, i) => idxs.contains(i)}.map(_._1)
  }

  def ReadDatasets(datasets: Array[String]): Array[Seq[String]] = {
    datasets.map(ReadDataset)
  }

  def ReadDataset(dataset: String): Seq[String] = {
    utils.FileUtils.readDataset(dataset)
  }

  def WriteToFile(f: File, d: Seq[String]): String = {
    val bw = new BufferedWriter(new FileWriter(f))
    bw.write(d.mkString("\n"))
    bw.close()
    f.getPath
  }

  def WriteDatasets(datasets: Array[Seq[String]], path: String): Array[String] = {
    val filenames = datasets.indices.map(i => s"$path/dataset_$i")
    val files = filenames.map(new File(_))
    filenames.foreach{ case f =>
      if(new File(f).exists()){
        deleteDir(new File(f))
      }}
    files.foreach(_.mkdirs())
    filenames
      .zip(datasets)
      .map{case (f, d) =>
        WriteToFile(new File(s"$f/part-00000"), d)
        f
      }.toArray
  }

  def Probe(program: InstrumentedProgram, probing_dataset: Array[Seq[String]]): (ProvInfo, Long, Long) = {
    val t_start = System.currentTimeMillis()
    val prov_info = program.main(program.args)
    (prov_info, t_start, System.currentTimeMillis())
  }

  def CreateProbingDatasets(schema: Array[Array[Schema[Any]]]): Array[Seq[String]] = {
    Array(
        Seq(
        """32120,PG0425,"2017-08-03 09:55:00.000","2017-08-03 12:35:00.000",SGC,VKT,Arrived,CN1,"2017-08-03 09:58:00.000","2017-08-03 12:39:00.000"""",
        """32123,PG0425,"2017-07-21 09:55:00.000","2017-07-21 12:35:00.000",SGC,VKT,Arrived,CN1,"2017-07-21 09:57:00.000","2017-07-21 12:38:00.000"""",
        """32121,PG0424,"2017-07-21 05:20:00.000","2017-07-21 08:00:00.000",SGC,VKT,Arrived,CN1,"2017-07-21 05:24:00.000","2017-07-21 08:04:00.000"""",
        """32122,PG0424,"2017-08-03 05:20:00.000","2017-08-03 08:00:00.000",SGC,VKT,Arrived,CN1,"2017-08-03 05:22:00.000","2017-08-03 07:59:00.000"""",
        """32123,PG0425,"2017-07-21 09:55:00.000","2017-07-21 12:35:00.000",SGC,VKT,Arrived,CN1,"2017-07-21 09:57:00.000","2017-07-21 12:38:00.000""""
      ),
      Seq(
        """UUD,"Ulan-Ude Airport (Mukhino)",Ulan-ude,107.438003540039,51.8078002929687,Asia/Irkutsk""",
          """MMK,"Murmansk Airport",Murmansk,32.7508010864258,68.7817001342773,Europe/Moscow""",
          """ABA,"Abakan Airport",Abakan,91.3850021362305,53.7400016784668,Asia/Krasnoyarsk""",
          """BAX,"Barnaul Airport",Barnaul,83.5384979248047,53.3638000488281,Asia/Krasnoyarsk""",
          """AAQ,"Anapa Vityazevo Airport",Anapa,37.347301483154,45.002101898193,Europe/Moscow""",
          """CNN,"Chulman Airport",Neryungri,124.91400146484,56.913898468018,Asia/Yakutsk"""

      )
    )
  }

  def blockFor[T](f: () => T, millis: Int): T = {
    val v = f()
    Thread.sleep(millis)
    v
  }

  def timeoutWrapper[T](func: () => T, timeout: Int, ret: T): T = {
    try {
      Await.result(Future(blocking(func())), duration.Duration(timeout, "sec"))
    } catch {
      case _ => ret
    }
  }

}
