package runners

import examples.{benchmarks, faulty, fuzzable, monitored}
import fuzzer.{ProvInfo, Schema}
import schemas.BenchmarkSchemas

object Config {

  val maxSamples = 5
  val maxRepeats = 1
  val iterations = 10
  val fuzzDuration = 10 // 86400 // duration in seconds
  val benchmarkName = "WebpageSegmentation"
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
    "DeliveryFaults" -> Array("seeds/weak_seed/deliveryfaults/deliveries")
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
    "WebpageSegmentation" -> Array("seeds/reduceddata/webpage_segmentation/before", "seeds/reduceddata/webpage_segmentation/after"),
    "CommuteType" -> Array("seeds/reduceddata/trips"),
    "Customers" -> Array("seeds/reduceddata/customers/customers", "seeds/reduceddata/customers/orders"),
    "FlightDistance" -> Array("seeds/reduceddata/LongFlights/flights", "seeds/reduceddata/LongFlights/airports"),
    "DeliveryFaults" -> Array("seeds/reduceddata/deliveries"),
    "Delays" -> Array("seeds/reduceddata/delays/station1", "seeds/reduceddata/delays/station2")
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
    "DeliveryFaults" -> Switch(fuzzable.DeliveryFaults.main, faulty.DeliveryFaults.main, faultTest)
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
    "DeliveryFaults" -> BenchmarkSchemas.FAULTS
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
