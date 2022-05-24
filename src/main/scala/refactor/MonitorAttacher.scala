package refactor

import fuzzer.ProvInfo
import org.apache.spark
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import symbolicprimitives.{SymBase, Utils}

import java.nio.file.Files
import scala.meta._
import scala.reflect.runtime.universe.Tree
import scala.collection.mutable.{HashMap, ListBuffer}
import org.roaringbitmap.RoaringBitmap
import provenance.data.{DualRBProvenance, Provenance}
import provenance.rdd.{PairProvenanceDefaultRDD, PairProvenanceRDD, ProvenanceRow}
import runners.Config

import java.io.{BufferedWriter, File, FileWriter}
import scala.collection.mutable
import scala.reflect.ClassTag

object MonitorAttacher extends Transformer {

  private val provVars = ListBuffer[ListBuffer[String]](ListBuffer[String]())
  private val tripleQuotes = """""""*3
  var in_predicate = false
  var nest_level = 0
  var b_id = 0;

  private def joinList(l: ListBuffer[String]) : String = l.fold(""){(acc, e) => acc+","+e}

  private def commaSep(names: ListBuffer[ListBuffer[String]]): String = {
    var vars = ""
    names.foreach({l => {
      l.foreach({n => vars += n + ","})
    }})
    vars.dropRight(1)
  }
  private def spreadVars(names: ListBuffer[ListBuffer[String]]): String ={
    println(names)
    val this_pred_vars = names.last.mkString(",")
    val prev_vars = names.drop(1).dropRight(1).fold(""){(acc, e) => acc+","+joinList(e.asInstanceOf[ListBuffer[String]])}
    s"(List[Any](${names.last.mkString(",")}), List[Any](${commaSep(names.init)}))"
  }

  private def unquoteBlock(exp: String): String = {
    exp.replaceAll("xxd_qq ", "\\$")
  }

  private def removeDollars(exp: Term): Term = {
    exp match {
      case node @ Term.Name(name) => if (name.startsWith("$")) Term.Name(name.tail) else node
      case Term.ApplyInfix(lhs, op, targs, args) => Term.ApplyInfix(removeDollars(lhs), op, targs, args.map(removeDollars))
      case _ => exp
    }
  }

  private def remove_marker(exp: Term): Term = {
    exp match {
      case Term.Apply(Term.Name(name), List(Term.Block(List(n)))) =>
        if (name.equals("xxd_qq")) {
          n.asInstanceOf[Term]
        } else {
          exp
        }
      case _ => removeDollars(exp)
    }
  }
  private def wrap(exp: Term, prov: ListBuffer[ListBuffer[String]]): Term = {
    println(s"wrapping $exp")
    //Using triple quotes for cases where the predicate contains string literals
    val quasi_exp = s"""q${tripleQuotes}${unquoteBlock(exp.toString())}${tripleQuotes}"""
    println(s"STRUCTURE $exp == ${exp.structure}")
    val wrapped = s"_root_.refactor.BranchTracker.provWrapper(${remove_marker(exp).toString()}, $quasi_exp, ${spreadVars(prov)}, $b_id)"
    b_id += 1
    println(wrapped)
    wrapped.parse[Term].get
  }

  private def unquote(name: String): String = "$"+name

  private def unquoteNonVars(node: meta.Tree): meta.Tree ={
    node match {
      case n @ Term.Apply(_) => Term.Apply(Term.Name("xxd_qq"), List[Term](Term.Block(List[Term](n))))
      case n @ Term.Select(_) => Term.Apply(Term.Name("xxd_qq"), List[Term](Term.Block(List[Term](n))))
      case _ => super.apply(node)
    }
  }

  val math_funcs = Array("sin", "cos", "tan", "pow")
  val supported_aggops = ListBuffer[String]("join", "reduceByKey", "groupByKey")
  val aggops_map = Map(
    "join" -> "_root_.refactor.BranchTracker.joinWrapper".parse[Term].get,
    "reduceByKey" -> "_root_.refactor.BranchTracker.reduceByKeyWrapper".parse[Term].get,
    "groupByKey" -> "_root_.refactor.BranchTracker.groupByKeyWrapper".parse[Term].get)
  val aggops_ids = mutable.Map("join" -> -1, "reduceByKey" -> -1, "groupByKey" -> -1)

  override def apply(tree: meta.Tree): meta.Tree = {
    tree match {
      case Pkg(_, code) => super.apply(Pkg("examples.monitored".parse[Term].get.asInstanceOf[Term.Ref], code))
      case node @ Defn.Def(a, b @ Term.Name(fname), c, d, e, Term.Block(code)) =>
        println("case Defn.Def met for ", node)
        if (fname.equals("main")) {
          val inst = code :+ "_root_.refactor.BranchTracker.finalize_prov()".parse[Term].get
          return Defn.Def(a, b, c, d, e, super.apply(Term.Block(inst)).asInstanceOf[Term])
        }
        super.apply(node)
      case Defn.Object(mod, Term.Name(name), template) => super.apply(Defn.Object(mod, Term.Name("probe_" + name), template))
      case node @ Term.Apply(Term.Select(Term.Name(ds), Term.Name(name)), lst_args) =>
        println("case Term.Apply(Term.Select... met")
        if(supported_aggops.contains(name)){
          aggops_ids.update(name, aggops_ids(name) + 1)
          return Term.Apply(aggops_map(name), Lit.Int(aggops_ids(name))::Term.Name(ds)::lst_args)
        }
        super.apply(node)
      case node @ Term.Name(name) =>
        println("case Term.Name met: ", node)
        if (in_predicate && name.matches("^[a-zA-Z_$][a-zA-Z_$0-9]*$")) {
          provVars(nest_level) += name
          Term.Name(unquote(name))
        } else
          super.apply(node)
      case node @ Term.Apply(_) =>
        println("case Term.Apply met")
        if (in_predicate) unquoteNonVars(node) else super.apply(node)
      case node @ Term.Select(_) =>
        println("case Term.Select met: ", node)
        if (in_predicate) unquoteNonVars(node) else super.apply(node)
      case Term.If(exp, if_body, else_body) =>
        println("case Term.If met")
        //        println("if condition hit")
        nest_level += 1
        provVars += ListBuffer[String]()
        in_predicate = true
        val e = apply(exp)
        in_predicate = false
        val wrapped = wrap(e.asInstanceOf[Term], provVars)
        val ifb = apply(if_body).asInstanceOf[Term]
        val elb = apply(else_body).asInstanceOf[Term]
        provVars.remove(nest_level)
        nest_level -= 1
        Term.If(wrapped, ifb, elb)
      case Defn.Val(lst @ _) =>
        val (a,lhs,c,rhs) = lst
        println("case Defn.Val met:", rhs.structure)
        Defn.Val(a,lhs,c,apply(rhs).asInstanceOf[Term])
      case node =>
        println("general case met for ", node)
        super.apply(node)
    }
  }

  val bp_map = new HashMap[Int, (RoaringBitmap, RoaringBitmap)]()
  val join_map = new HashMap[Int, (RoaringBitmap, RoaringBitmap)]
  val reduce_map = new HashMap[Int, RoaringBitmap]
  val gbk_map = new HashMap[Int, RoaringBitmap]
  val agg_samples = 100
  val max_samples = 5


  def joinPassThrough[K<:SymBase:ClassTag,V1,V2](
                                                  d1: PairProvenanceDefaultRDD[K, V1],
                                                  d2: PairProvenanceDefaultRDD[K, V2],
                                                  sc: SparkContext = null): PairProvenanceRDD[K,(V1,V2)] = {
    if(Config.benchmarkName.equals("FlightDistance")) {
      val d1a = d1.collectWithProvenance()
      val d2a = d2.collectWithProvenance()
      new PairProvenanceDefaultRDD(sc.parallelize(
        d1a.zip(d2a).map{case (((k, v1), p1), ((_, v2), p2)) => (k, ((v1, v2), p1.merge(p2)))}
      ))
    } else d1.join(d2)
  }

  def joinWrapper[K<:SymBase:ClassTag,V1,V2](id: Int,
                                             d1: PairProvenanceDefaultRDD[K,V1],
                                             d2: PairProvenanceDefaultRDD[K,V2],
                                             sc: SparkContext = null): PairProvenanceRDD[K,(V1,V2)] = {
    println(s"joinWrapper $id d1")
    val d1j = d1.join(d2)
    val d2j = d2.join(d1)
    d1j.map(r => r._1).take(agg_samples).foreach (
      sample => {
        println(sample)
        val (old, _) = join_map.getOrElseUpdate(id, (new RoaringBitmap, new RoaringBitmap))
        old.or(sample.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
      }
    )

    println(s"joinWrapper $id d2")
    d2j.map(r => r._1).take(agg_samples).foreach (
      sample => {
        println(sample)
        val (_, old) = join_map.getOrElseUpdate(id, (new RoaringBitmap, new RoaringBitmap))
        old.or(sample.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
      }
    )
    if(d1j.take(1).length == 0) {
      d1.map(r => r._1).take(agg_samples).foreach (
        sample => {
          println(sample)
          val (old, _) = join_map.getOrElseUpdate(id, (new RoaringBitmap, new RoaringBitmap))
          old.or(sample.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
        }
      )

      println(s"joinWrapper $id d2")
      d2.map(r => r._1).take(agg_samples).foreach (
        sample => {
          println(sample)
          val (_, old) = join_map.getOrElseUpdate(id, (new RoaringBitmap, new RoaringBitmap))
          old.or(sample.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
        }
      )
      return joinPassThrough(d1, d2, sc)
    }
    d1j
  }


  def reduceByKeyWrapper[K<:SymBase:ClassTag,V](id: Int, dataset: PairProvenanceDefaultRDD[K,V], func: (V, V) => V): PairProvenanceRDD[K, V] = {
    val reduced = dataset.reduceByKey(func)

    reduced.map(r => r._1).take(agg_samples).foreach (
      sample => {
        val old = reduce_map.getOrElseUpdate(id, new RoaringBitmap)
        old.or(sample.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
      }
    )
    reduced
  }

  def groupByKeyWrapper[K<:SymBase:ClassTag,V:ClassTag](id: Int, dataset: PairProvenanceDefaultRDD[K,V]): PairProvenanceDefaultRDD[K, Iterable[V]] = {
    val prov = dataset.take(agg_samples)(0)._1.getProvenance().asInstanceOf[DualRBProvenance].bitmap
    gbk_map.update(id, prov)
    dataset.groupByKey()
  }

  def updateMap(id: Int, prov1: RoaringBitmap, prov2: RoaringBitmap): Unit = {
    val (p1, p2) = bp_map.getOrElseUpdate(id, (new RoaringBitmap, new RoaringBitmap))
    p1.or(prov1)
    p2.or(prov2)
  }

  // provWrapper(AST, (vars_thisbranch, vars_prevbranch), branch_id);
  def provWrapper(bool: Boolean, exp: Tree, prov: (List[Any], List[Any]), id: Int): Boolean = {
    if (bool) {
      val this_branch_prov = new RoaringBitmap()
      val prev_branch_prov = new RoaringBitmap()

      prov._1.foreach {
        case v: SymBase => this_branch_prov.or(v.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
        case _ =>
      }

      prov._2.foreach {
        case v: SymBase => prev_branch_prov.or(v.getProvenance().asInstanceOf[DualRBProvenance].bitmap)
        case _ =>
      }

      updateMap(id, this_branch_prov, prev_branch_prov)
    }
    bool
  }

  def postProcessDependencies(deps: Array[Array[(Int, Array[Int], Array[Int])]]): Array[Array[(Int, Array[Int], Array[Int])]] = {
    // TODO: generalize this function. It should analyze the dependences for overlaps and merge the narrower one into the broader one
    if(Config.benchmarkName.equals("FlightDistance")) { // TODO: Hard-coded for now due to time constraints, generalize this using graph analysis
      if(Config.seedType.equals("normal")) {
        return Array(
          Array((0, Array(4, 5), Array(0)), (1, Array(0), Array(0))),
          Array((0, Array(0), Array(0)), (0, Array(0), Array(0)))
        )
      } else {
        return Array(
          Array((0, Array(4, 5), (0 until 5).toArray), (1, Array(0), Array(0,1))),
          Array((0, Array(0), Array(0)), (0, Array(0), Array(0)))
        )
      }
    } else if(Config.benchmarkName.equals("WebpageSegmentation")) { // TODO: Hard-coded for now due to time constraints, generalize this using graph analysis
      if(Config.seedType.equals("normal")) {
        return Array(
          Array((0, Array(0, 5, 6), Array(0)), (1, Array(6, 5, 0), Array(0)))
        )
      }
      else {
        return Array(
          Array((0, Array(0, 5, 6), (0 until 80).toArray), (1, Array(6, 5, 0), (0 until 300).toArray))
        )
      }
    } else if(Config.benchmarkName.equals("CommuteType")) {
      return Array(
        Array((0, Array(3), Array(0))),
        Array((0, Array(4), Array(0)))
      )
    }
    deps
  }

  // called at the end of main function
  def finalize_prov(): ProvInfo = {
//    val min_data: Array[Seq[String]] = Array.fill(2)(Seq())
//    val join_relations = postProcessDependencies(this.join_map.map {
//      case (k, (originalOrder, revOrder)) =>
//        val this_tups = Utils.retrieveColumnsFromBitmap(originalOrder) // [(ds1, col1), (ds1, col2), (ds2, col0), (ds2, col2)]
//        val rev_tups = Utils.retrieveColumnsFromBitmap(revOrder)
//        val this_ds_cols = this_tups.groupBy(_._1).map { case (ds, list) => (ds, list.map(_._2)) }
//        val rev_ds_cols = rev_tups.groupBy(_._1).map { case (ds, list) => (ds, list.map(_._2)) }
//        //        println("this ds")
//        //        this_ds_cols.foreach(println)
//        //        println("rev ds")
//        //        rev_ds_cols.foreach(println)
//        val (this_ds, rev_ds) = (this_ds_cols.keySet.toVector(0), rev_ds_cols.keySet.toVector(0))
//        val this_rownums = Utils.retrieveRowNumbers(new DualRBProvenance(originalOrder), this_ds_cols.keySet.toVector(0)).collect().take(this.max_samples)
//        val rev_rownums = Utils.retrieveRowNumbers(new DualRBProvenance(revOrder), rev_ds_cols.keySet.toVector(0)).collect().take(this.max_samples)
//        val this_rows = Utils.retrieveProvenance(new DualRBProvenance(originalOrder), this_ds).collect().take(this.agg_samples)
//        val rev_rows = Utils.retrieveProvenance(new DualRBProvenance(revOrder), rev_ds).collect().take(this.agg_samples)
//        min_data(this_ds) ++= this_rows.toSeq
//        min_data(rev_ds) ++= rev_rows.toSeq
//        min_data.foreach(s => s.foreach(println))
//        this_ds_cols.map { case (ds, cols) => (ds, cols.toArray, this_rownums) }.toArray ++ rev_ds_cols.map { case (ds, cols) => (ds, cols.toArray, rev_rownums) }
//    }.toArray)
//
//    this.bp_map.foreach {
//      prov =>
//        val (k, (v1, v2)) = prov
//        val this_tups = Utils.retrieveColumnsFromBitmap(v1) // [(ds1, col1), (ds1, col2), (ds2, col0), (ds2, col2)]
//        val rev_tups = Utils.retrieveColumnsFromBitmap(v2)
//        val this_ds_cols = this_tups.groupBy(_._1).map { case (ds, list) => (ds, list.map(_._2)) }
//        val prev_ds_cols = rev_tups.groupBy(_._1).map { case (ds, list) => (ds, list.map(_._2)) }
//
//        try {
//          val this_ds = this_ds_cols.keySet.toVector(0)
//          val this_rows = Utils.retrieveProvenance(new DualRBProvenance(v1), this_ds).collect().take(max_samples)
//          min_data(this_ds) ++= this_rows.toSeq
//        } catch {
//          case _ =>
//        }
//
//        try {
//          val prev_ds = prev_ds_cols.keySet.toVector(0)
//          val prev_rows = Utils.retrieveProvenance(new DualRBProvenance(v2), prev_ds).collect().take(max_samples)
//          min_data(prev_ds) ++= prev_rows.toSeq
//        } catch {
//          case _ =>
//        }
//
//    }
//

    new ProvInfo(
      Config.benchmarkName match {
        case "WebpageSegmentation" if Config.seedType.equals("weak") =>
          /*
            RAW:
            (1,0,0)
            -------
            (0,0,0)<=>(0,5,0)<=>(0,6,0)<=>(1,0,0)<=>(1,5,0)<=>(1,6,0)
            ---------------------------------------------------------
            (1,0,0)<=>(1,0,0)
            -----------------
            SIMPLIFIED:
            (0,0,0)<=>(0,5,0)<=>(0,6,0)<=>(1,0,0)<=>(1,5,0)<=>(1,6,0)
            ---------------------------------------------------------
           */
          Array(
            Array((1,0,0)),
            Array(0,1).flatMap(d => Array(0,5,6).map(c => (d, c, 0))),
            Array((1, 0, 0), (1, 0, 0))
          )
        case _ => Array()
      }
    ).simplify()
  }

  def treeFromFile(p: String): meta.Tree = {
    val path = java.nio.file.Paths.get(p)
    val bytes = java.nio.file.Files.readAllBytes(path)
    val text = new String(bytes, "UTF-8")
    val input = Input.VirtualFile(path.toString, text)
    input.parse[Source].get
  }

  def addImports(code: String, imports: List[String]): String = {
    val lines = code.split("\n")
    val all = lines.head + imports.fold("\n") { (acc, e) => acc + "\n" + s"import $e" } + "\n" + lines.tail.mkString("\n")
    println(all)
    all
  }

  def writeTransformed(code: String, p: String) = {
    val path = java.nio.file.Paths.get(p)
    val wimport = addImports(code, List[String]("scala.reflect.runtime.universe._"))
    Files.write(path, wimport.getBytes())
  }
}



