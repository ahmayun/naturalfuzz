package runners

import fuzzer.{Fuzzer, Program}
import guidance.BigFuzzGuidance
import scoverage.report.ScoverageHtmlWriter
import scoverage.{IOUtils, Serializer}
import symbolicexecution.SymbolicExecutor
import utils.RIGUtils
import utils.TimingUtils.timeFunction

import java.io.File

object RunBigFuzz {

  def main(args: Array[String]): Unit = {

    val runs = Config.iterations

    // ==P.U.T. dependent configurations=======================
    val benchmarkName = Config.benchmarkName
    val Some(inputFiles) = Config.mapInputFiles.get(benchmarkName)
    val Some(funFuzzable) = Config.mapFunFuzzables.get(benchmarkName)
    val Some(schema) = Config.mapSchemas.get(benchmarkName)
    val benchmarkClass = Config.benchmarkClass
    // ========================================================

    val guidance = new BigFuzzGuidance(inputFiles, schema, runs)
    val benchmarkPath = s"src/main/scala/${benchmarkClass.split('.').mkString("/")}.scala"
    val coverageOutDir = Config.scoverageResultsDir
    val program = new Program(benchmarkName,
      benchmarkClass,
      benchmarkPath,
      funFuzzable,
      inputFiles)

    // Preprocessing and Fuzzing
    val (pathExpressions, timeSymbolicExecution) = timeFunction(() => SymbolicExecutor.execute(program))
    val (filterQueries, timeQueryConstruction) = timeFunction(() => RIGUtils.createFilterQueries(pathExpressions))
    val (queryRDDs, timeFilter) = timeFunction(() => filterQueries.getRows(program.args))
    val (generatedInput, timeMixMatch) = timeFunction(() => queryRDDs.mixMatch())


    val coverage = Serializer.deserialize(new File(s"$coverageOutDir/scoverage.coverage"))
    val measurementFiles = IOUtils.findMeasurementFiles(coverageOutDir)
    val measurements = IOUtils.invoked(measurementFiles)

    coverage.apply(measurements)
    new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(coverageOutDir)).write(coverage)


  }

}
