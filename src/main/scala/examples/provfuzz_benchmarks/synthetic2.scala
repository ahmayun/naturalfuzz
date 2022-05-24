package examples.provfuzz_benchmarks

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import provenance.data.Provenance
import sparkwrapper.SparkContextWithDP
import symbolicprimitives.{SymInt, SymString}

object synthetic2 {

  def main(args: Array[String]): Unit = {
    //set up spark configuration
    val sparkConf = new SparkConf()
    sparkConf.setMaster("local[6]")
    sparkConf.setAppName("Column Provenance Test").set("spark.executor.memory", "2g")
    val customers_data = "datasets/pfbenchmark2_data/customers"
    val orders_data = "datasets/pfbenchmark2_data/orders"
    val ctx = new SparkContext(sparkConf) //set up lineage context and start capture lineage
    ctx.setLogLevel("ERROR")
    Provenance.setProvenanceType("dual")
    val scdp = new SparkContextWithDP(ctx)
    val orders = scdp.textFileProv(orders_data, x=>x.split(","))
    val customers = scdp.textFileProv(customers_data, x=>x.split(","))
    //  ---------------------------------------------------------------------------------------

    // sample data point customers:
    //  CustomerID	CustomerName	ContactName	Country
    //  1,Alfreds Futterkiste,Maria Anders,Germany
    // sample data point orders:
    //  OrderID	CustomerID	OrderDate
    //  10308,2,1996-09-18

    //        println("ORDERS:")
    //        orders.foreach(arr => {
    //          arr.foreach(s => {print(s); print(", ")})
    //          println()
    //        })
    //        println("CUSTOMERS:")
    //        customers.foreach(arr => {
    //          arr.foreach(s => {print(s); print(", ")})
    //          println()
    //        })

    val o = orders
      .map{
        row =>
          (row(1), row(2))
      }

    val c = customers
      .map{
        row =>
          (row(3)+row(4), (row(0), row(1), row(2)))
      }

    println("ORDERS:")
    o.collect().foreach(println)
    println("CUSTOMERS:")
    c.collect().foreach(println)

    //    val joined = c.join(o)
    val joined = c.join(o)

        println("RDD AFTER JOIN:")
        joined.collectWithProvenance().foreach(println)
        println("___")

    //    println("PROVENANCE:")
    //    joined.collectWithProvenance().map(r=>r._2).foreach(
    //      prov => {
    //        println(prov)
    //        println(Utils.retrieveProvenance(prov))
    //      }
    //    )

    val filtered = joined.filter{
      e =>
        val (k, _) = e
        threes(k)
    }
    val mapped = filtered.map{
      e =>
        val (k, (_, b)) = e
        (k, b.split("-")(0).toInt)
    }

    mapped.reduceByKey((r1:SymInt,r2:SymInt) => {
      r1+r2
    }).collect().foreach(println)

  }

  def threes(v: SymString):Boolean = {
    if(v == "33"){
      return true
    }
    false
  }

}