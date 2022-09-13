package runners

import fuzzer.{Fuzzer, Program, SymbolicProgram}
import guidance.RIGGuidance
import runners.RunNormal.limitDP
import scoverage.report.ScoverageHtmlWriter
import scoverage.{IOUtils, Serializer}
import symbolicexecution.SymbolicExecutor
import utils.{QueriedRDDs, QueryResult, RIGUtils}
import utils.TimingUtils.timeFunction

import java.io.File

object RunRIGFuzz {

  def main(args: Array[String]): Unit = {

    val runs = Config.iterations

    // ==P.U.T. dependent configurations=======================
    val benchmarkName = Config.benchmarkName
    val Some(inputFiles) = Config.mapInputFiles.get(benchmarkName)
    val Some(funFuzzable) = Config.mapFunFuzzables.get(benchmarkName)
    val Some(funSymEx) = Config.mapFunSymEx.get(benchmarkName)
    val Some(schema) = Config.mapSchemas.get(benchmarkName)
    val benchmarkClass = Config.benchmarkClass
    // ========================================================

    val benchmarkPath = s"src/main/scala/${benchmarkClass.split('.').mkString("/")}.scala"

    val outputDir = s"${Config.resultsDir}/RIGFuzz"
    val scoverageResultsDir = s"$outputDir/scoverage-results"

    val program = new Program(
      benchmarkName,
      benchmarkClass,
      benchmarkPath,
      funFuzzable,
      inputFiles)

    val symProgram = new SymbolicProgram(
      benchmarkName,
      benchmarkClass,
      benchmarkPath,
      funSymEx,
      inputFiles)

    // Preprocessing and Fuzzing
    val pathExpressions = SymbolicExecutor.execute(symProgram)
    val filterQueries = RIGUtils.createFilterQueries(pathExpressions)
    filterQueries.filterQueries.zipWithIndex.foreach {case (q, i) => println(i, q.tree)}
    val satRDDs = filterQueries.createSatVectors(program.args) // create RDD with bit vector and bit counts
    val minSatRDDs = satRDDs.getRandMinimumSatSet()
    val brokenRDDs: List[QueryResult] = minSatRDDs.breakIntoQueryRDDs() // not ideal, but allows me to leverage my own existing code


//    val (queryRDDs, _) = timeFunction(() => filterQueries.getRows(program.args))
    val guidance = new RIGGuidance(inputFiles, schema, runs, new QueriedRDDs(brokenRDDs))

    val (stats, _, _) = Fuzzer.Fuzz(program, guidance, outputDir)
    
    val coverage = Serializer.deserialize(new File(s"$scoverageResultsDir/scoverage.coverage"))
    val measurementFiles = IOUtils.findMeasurementFiles(scoverageResultsDir)
    val measurements = IOUtils.invoked(measurementFiles)

    coverage.apply(measurements)
    new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(scoverageResultsDir)).write(coverage)
    println("=== qrdds ====")
    brokenRDDs.foreach{
      q =>
        println("===")
        println(q.toString)
    }
    println(s"Coverage: ${limitDP(coverage.statementCoveragePercent, 2)}% (gathered from ${measurementFiles.length} measurement files)")

  }

}
