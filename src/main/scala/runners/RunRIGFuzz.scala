package runners

import fuzzer.{Fuzzer, Program}
import guidance.RIGGuidance
import runners.RunNormal.limitDP
import scoverage.report.ScoverageHtmlWriter
import scoverage.{IOUtils, Serializer}
import symbolicexecution.SymbolicExecutor
import utils.{QueriedRDDs, RIGUtils}
import utils.TimingUtils.timeFunction

import java.io.File

object RunRIGFuzz {

  def main(args: Array[String]): Unit = {

    val runs = Config.iterations

    // ==P.U.T. dependent configurations=======================
    val benchmarkName = Config.benchmarkName
    val Some(inputFiles) = Config.mapInputFiles.get(benchmarkName)
    val Some(funFuzzable) = Config.mapFunFuzzables.get(benchmarkName)
    val Some(schema) = Config.mapSchemas.get(benchmarkName)
    val benchmarkClass = Config.benchmarkClass
    // ========================================================

    val benchmarkPath = s"src/main/scala/${benchmarkClass.split('.').mkString("/")}.scala"
    val coverageOutDir = Config.scoverageResultsDir
    val program = new Program(benchmarkName,
      benchmarkClass,
      benchmarkPath,
      funFuzzable,
      inputFiles)

    // Preprocessing and Fuzzing
    val (pathExpressions, _) = timeFunction(() => SymbolicExecutor.execute(program))
    val (filterQueries, _) = timeFunction(() => RIGUtils.createFilterQueries(pathExpressions))
    val (queryRDDs, _) = timeFunction(() => filterQueries.getRows(program.args))
    val guidance = new RIGGuidance(inputFiles, schema, runs, new QueriedRDDs(queryRDDs))

    val (stats, _, _) = Fuzzer.Fuzz(program, guidance, coverageOutDir)


    val coverage = Serializer.deserialize(new File(s"$coverageOutDir/scoverage.coverage"))
    val measurementFiles = IOUtils.findMeasurementFiles(coverageOutDir)
    val measurements = IOUtils.invoked(measurementFiles)

    coverage.apply(measurements)
    new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(coverageOutDir)).write(coverage)
    println("=== qrdds ====")
    queryRDDs.foreach{
      q =>
        println("===")
        println(q.toString)
    }
    println(s"Coverage: ${limitDP(coverage.statementCoveragePercent, 2)}% (gathered from ${measurementFiles.length} measurement files)")

  }

}
