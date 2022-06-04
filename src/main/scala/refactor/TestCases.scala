package refactor

object TestCases {

  val testCases = Map(
    "predicateTest1" ->
      """
        |object predicateTest1 {
        |
        |  def main(args: Array[String]): Unit = {
        |   val x = 3
        |   if(x < 5) {
        |     println("do this")
        |   } else {
        |     println("do that")
        |   }
        |  }
        |}
        |""".stripMargin,
    "predicateTest2" ->
      """
        |package refactor.testout
        |
        |import org.apache.spark.{SparkConf, SparkContext}
        |
        |object predicateTest2 {
        |
        |  def main(args: Array[String]): Unit = {
        |    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("Test")
        |    val ctx = new SparkContext(sparkConf)
        |    val data1 = ctx.textFile("dummydata/predicateTest2").map{arr => arr.split(",")}
        |    val data2 = ctx.textFile("dummydata/predicateTest2").map{arr => arr.split(",")}
        |
        |    val d1 = data1.map(arr => (arr.head, arr.tail))
        |    val d2 = data2.map(arr => (arr.head, arr.tail))
        |
        |    val d3 = d1.join(d2).filter{ case (k, (_, _)) => k.toInt > 3 && k.toInt < 6}
        |    d3.groupByKey().foreach(println)
        |  }
        |
        |}
        |""".stripMargin
  )
}

/*
  private val provVars = ListBuffer[ListBuffer[String]](ListBuffer[String]())
  private val tripleQuotes = """""""*3
  var in_predicate = false
  var nest_level = 0
  var b_id = 0

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
          val inst = code :+ "_root_.refactor.BranchTracker.finalizeProvenance()".parse[Term].get
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
 */