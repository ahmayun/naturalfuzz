package utils

import scoverage.report.ScoverageHtmlWriter
import scoverage.{Coverage, ScoverageOptions, Serializer}

import java.io.File

object CompilerUtils {

  def CompileWithScoverage(path: String, coverage_out_dir: String) : Unit = {
    val scoverageOptions = new ScoverageOptions()
    scoverageOptions.dataDir = coverage_out_dir

    val compiler = ScoverageCompiler.default
    compiler.settings.outdir.value = "target/scala-2.11/classes"
    compiler.settings.Xprint.value = List()
    compiler.settings.Yposdebug.value = false
    compiler.setOptions(scoverageOptions)

    compiler.compileSourceFiles(new File(path))
  }

  def ProcessCoverage(output_dir: String, createHTML: Boolean = true): (Coverage, Int) = {

    val coverage = Serializer.deserialize(new File(s"$output_dir/scoverage.coverage"))
    val measurementFiles = IOUtils.findMeasurementFiles(output_dir)
    val measurements = scoverage.IOUtils.invoked(measurementFiles)

    coverage.apply(measurements)

    if(createHTML)
      new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(output_dir)).write(coverage)

    (coverage, measurementFiles.length)
  }
}
