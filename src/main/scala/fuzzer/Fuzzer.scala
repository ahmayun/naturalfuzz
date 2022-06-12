package fuzzer

import runners.Config
import scoverage.report.ScoverageHtmlWriter
import scoverage.{Constants, Coverage, IOUtils, Serializer}
import utils.CompilerUtils.CompileWithScoverage
import utils.FileUtils
import scala.reflect.io.Directory

import java.io.{File, FileFilter}

object Fuzzer {

  def getMeasurementFile(dataDir: File, iteration: Int): Array[File] = dataDir.listFiles(new FileFilter {
    override def accept(pathname: File): Boolean = pathname.getName.startsWith(s"${Constants.MeasurementsPrefix}")
  })

  def getCoverage(dataDir: String, iteration: Int): Coverage = {
    val coverage = Serializer.deserialize(new File(s"$dataDir/${Constants.CoverageFileName}"))
    val measurementFiles = getMeasurementFile(new File(dataDir), iteration)
//    println(measurementFiles.length)
    coverage.apply(IOUtils.invoked(measurementFiles))
    coverage
  }

  def writeToFile(outDir: String, data: Seq[String], dataset: Int) : String = {
    val output_dir = s"$outDir/dataset_$dataset"
    val output_file = s"$output_dir/part-000000"
    FileUtils.writeToFile(data, output_file)
    output_dir
  }

  def Fuzz(program: Program, guidance: Guidance, coverage_out_dir: String, testCaseOutDir: String = "target/fuzz-output"): (FuzzStats, Long, Long) = {
    new Directory(new File(testCaseOutDir)).deleteRecursively()
    val stats = new FuzzStats(program.name)
    var crashed = false
    CompileWithScoverage(program.classpath, coverage_out_dir)
    val t_start = System.currentTimeMillis()
    while(!guidance.isDone()) {
      val outDir = s"$testCaseOutDir/iter_${fuzzer.Global.iteration}"
      val inputDatasets = guidance.getInput().map(f => FileUtils.readDatasetPart(f, 0))
      val mutated_files = guidance.mutate(inputDatasets).zipWithIndex.map{case (e, i) => writeToFile(outDir, e, i)}
      try {
        program.main(mutated_files)
        crashed = false
      } catch {
        case e: Throwable =>
          crashed = true
          val trace = e.getStackTrace.mkString(",")
          val e_id = Vector(e.getClass.getCanonicalName, trace)
          if(Config.deepFaults && e.getClass.getCanonicalName.equals("java.lang.RuntimeException")) {
            stats.failures+=1
            stats.failureMap.update(e_id, {
              val (throwable, count, itr) = stats.failureMap.getOrElseUpdate(e_id, (e, 0, fuzzer.Global.iteration))
              (throwable, count+1, itr)
            })
            stats.cumulativeError :+= stats.failureMap.keySet.size
          }
          else if (!Config.deepFaults) {
            stats.failures+=1
            stats.failureMap.update(e_id, {
              val (throwable, count, itr) = stats.failureMap.getOrElseUpdate(e_id, (e, 0, fuzzer.Global.iteration))
              (throwable, count+1, itr)
            })
            stats.cumulativeError :+= stats.failureMap.keySet.size
          }

        case _ =>
      }
      val coverage = getCoverage(coverage_out_dir, fuzzer.Global.iteration)
      stats.add_plot_point(fuzzer.Global.iteration, coverage.statementCoveragePercent)

      if(!guidance.updateCoverage(coverage, crashed)){
        new Directory(new File(outDir)).deleteRecursively()
      }

      new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(coverage_out_dir)).write(coverage)
      fuzzer.Global.iteration += 1
    }

    if(stats.failureMap.keySet.size > Global.maxErrors) {
      Global.maxErrorsMap = stats.failureMap
      Global.maxErrors = stats.failureMap.keySet.size
    }
    println("inputs: " + guidance.getInput().mkString(", "))
    (stats, t_start, System.currentTimeMillis())
  }

}
