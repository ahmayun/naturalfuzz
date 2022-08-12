package runners

import examples.{benchmarks, faulty, fuzzable, monitored}
import fuzzer.{ProvInfo, Schema}
import schemas.BenchmarkSchemas

object Config {

  val maxSamples = 5
  val maxRepeats = 1
  val percentageProv = 0.001f
  val iterations = 10
  val fuzzDuration = 10 // 86400 // duration in seconds
  val benchmarkName = "FindSalary"
  val resultsDir = s"./target/fuzzer-results/$benchmarkName"
  val faultTest = true
  val deepFaults = false
  val seedType = "weak" //either full, reduced or weak
  val benchmarkClass = s"examples.${if (faultTest) "faulty" else "fuzzable"}.$benchmarkName"
  val mutateProbs: Array[Float] = Array( // 0:M1, 1:M2 ... 5:M6
    0.9f, // Data
    0.02f, // Data
    0.02f, // Format
    0.02f, // Format
    0.02f, // Format
    0.02f) // Format


  val mapInputFilesWeak: Map[String, Array[String]] = Map(
    "FlightDistance" -> Array("seeds/weak_seed/FlightDistance/flights", "seeds/weak_seed/FlightDistance/airports_data"),
    "WebpageSegmentation" -> Array("seeds/weak_seed/webpage_segmentation/before", "seeds/weak_seed/webpage_segmentation/after"),
    "CommuteType" -> Array("seeds/weak_seed/commute/trips"),
    "Delays" -> Array("seeds/weak_seed/delays/station1", "seeds/weak_seed/delays/station2"),
    "Customers" -> Array("seeds/weak_seed/orders/customers", "seeds/weak_seed/orders/orders"),
    "DeliveryFaults" -> Array("seeds/weak_seed/deliveryfaults/deliveries"),
    "StudentGrade" -> Array("seeds/weak_seed/studentgrade/grades"),
    "MovieRating" -> Array("seeds/weak_seed/movierating/ratings"),
    "NumberSeries" -> Array("seeds/weak_seed/numberseries/numbers"),
    "AgeAnalysis" -> Array("seeds/weak_seed/ageanalysis/ages"),
    "WordCount" -> Array("seeds/weak_seed/wordcount/words"),
    "ExternalCall" -> Array("seeds/weak_seed/externalcall/calls"),
    "FindSalary" -> Array("seeds/weak_seed/findsalary/salaries")
  )

  val mapInputFilesFull: Map[String, Array[String]] = Map(
    "WebpageSegmentation" -> Array("seeds/full_data/webpage_segmentation/before", "seeds/full_data/webpage_segmentation/after"),
    "CommuteType" -> Array("seeds/full_data/trips"),
    "Customers" -> Array("seeds/full_data/customers/customers", "seeds/full_data/customers/orders"),
    "FlightDistance" -> Array("seeds/full_data/LongFlights/flights", "seeds/full_data/LongFlights/airports"),
    "DeliveryFaults" -> Array("seeds/full_data/deliveries"),
    "Delays" -> Array("seeds/full_data/delays/station1", "seeds/full_data/delays/station2")
  )

  val mapInputFilesReduced: Map[String, Array[String]] = Map(
    "WebpageSegmentation" -> Array("seeds/reduced_data/webpage_segmentation/before", "seeds/reduced_data/webpage_segmentation/after"),
    "CommuteType" -> Array("seeds/reduced_data/trips"),
    "Customers" -> Array("seeds/reduced_data/customers/customers", "seeds/reduced_data/customers/orders"),
    "FlightDistance" -> Array("seeds/reduced_data/LongFlights/flights", "seeds/reduced_data/LongFlights/airports"),
    "DeliveryFaults" -> Array("seeds/reduced_data/deliveries"),
    "Delays" -> Array("seeds/reduced_data/delays/station1", "seeds/reduced_data/delays/station2")
  )

  val Some(mapInputFiles) = Map(
    "weak" -> mapInputFilesWeak,
    "reduced" -> mapInputFilesReduced,
    "full" -> mapInputFilesFull
  ).get(seedType)


  def Switch(normal: Array[String] => Unit, faulty: Array[String] => Unit, switch: Boolean): Array[String] => Unit = {
    if (switch) faulty else normal
  }

  val mapFunFuzzables: Map[String, Array[String] => Unit] = Map[String, Array[String] => Unit](elems =
    "FlightDistance" -> Switch(fuzzable.FlightDistance.main, faulty.FlightDistance.main, faultTest),
    "WebpageSegmentation" -> Switch(fuzzable.WebpageSegmentation.main, faulty.WebpageSegmentation.main, faultTest),
    "CommuteType" -> Switch(fuzzable.CommuteType.main, faulty.CommuteType.main, faultTest),
    "Delays" -> Switch(fuzzable.Delays.main, faulty.Delays.main, faultTest),
    "Customers" -> Switch(fuzzable.Customers.main, faulty.Customers.main, faultTest),
    "DeliveryFaults" -> Switch(fuzzable.DeliveryFaults.main, faulty.DeliveryFaults.main, faultTest),
    "StudentGrade" -> Switch(null, faulty.StudentGrade.main, faultTest),
    "MovieRating" -> Switch(null, faulty.MovieRating.main, faultTest),
    "NumberSeries" -> Switch(null, faulty.NumberSeries.main, faultTest),
    "AgeAnalysis" -> Switch(null, faulty.AgeAnalysis.main, faultTest),
    "WordCount" -> Switch(null, faulty.WordCount.main, faultTest),
    "ExternalCall" -> Switch(null, faulty.ExternalCall.main, faultTest),
    "FindSalary" -> Switch(null, faulty.FindSalary.main, faultTest)
  )


  val mapFunSpark: Map[String, Array[String] => Unit] = Map[String, Array[String] => Unit](elems =
    "FlightDistance" -> benchmarks.FlightDistance.main,
    "WebpageSegmentation" -> benchmarks.WebpageSegmentation.main,
    "CommuteType" -> benchmarks.CommuteType.main,
    "Delays" -> benchmarks.Delays.main,
    "Customers" -> benchmarks.Customers.main,
    "DeliveryFaults" -> benchmarks.DeliveryFaults.main
  )

  val mapFunProbeAble: Map[String, Array[String] => ProvInfo] = Map[String, Array[String] => ProvInfo](elems =
    "FlightDistance" -> monitored.FlightDistance.main,
    "WebpageSegmentation" -> monitored.WebpageSegmentation.main,
    "CommuteType" -> monitored.CommuteType.main,
    "Delays" -> monitored.Delays.main,
    "Customers" -> monitored.Customers.main,
    "DeliveryFaults" -> monitored.DeliveryFaults.main
  )

  val mapSchemas: Map[String, Array[Array[Schema[Any]]]] = Map[String, Array[Array[Schema[Any]]]](elems =
    "FlightDistance" -> BenchmarkSchemas.SYNTHETIC3,
    "WebpageSegmentation" -> BenchmarkSchemas.SEGMENTATION,
    "CommuteType" -> BenchmarkSchemas.COMMUTE,
    "Delays" -> BenchmarkSchemas.DELAYS,
    "Customers" -> BenchmarkSchemas.CUSTOMERS,
    "DeliveryFaults" -> BenchmarkSchemas.FAULTS,
    "StudentGrade" -> BenchmarkSchemas.STUDENTGRADE,
    "MovieRating" -> BenchmarkSchemas.MOVIERATING,
    "NumberSeries" -> BenchmarkSchemas.NUMBERSERIES,
    "AgeAnalysis" -> BenchmarkSchemas.AGEANALYSIS,
    "WordCount" -> BenchmarkSchemas.WORDCOUNT,
    "ExternalCall" -> BenchmarkSchemas.EXTERNALCALL,
    "FindSalary" -> BenchmarkSchemas.FINDSALARY
  )

  val mapErrorCountAll: Map[String, Int] = Map[String, Int](elems =
    "FlightDistance" -> 7,
    "WebpageSegmentation" -> 10,
    "CommuteType" -> 6,
    "Delays" -> 10,
    "Customers" -> 10,
    "DeliveryFaults" -> 7
  )

  val mapErrorCountDeep: Map[String, Int] = Map[String, Int](elems =
    "FlightDistance" -> 3,
    "WebpageSegmentation" -> 5,
    "CommuteType" -> 3,
    "Delays" -> 6,
    "Customers" -> 3,
    "DeliveryFaults" -> 2
  )

  val Some(map_err_count) = Map(elems =
    false -> mapErrorCountAll,
    true -> mapErrorCountDeep
  ).get(deepFaults)
}
