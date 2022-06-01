package refactor

import java.nio.file.Files
import scala.meta.{Defn, Init, Input, Name, Source, Stat, Term, Transformer, Tree, Type, XtensionParseInputLike}
import refactor.Constants

object MonitorAttacher extends Transformer {

  var id = -1
  def treeFromFile(p: String): Tree = {
    val path = java.nio.file.Paths.get(p)
    val bytes = java.nio.file.Files.readAllBytes(path)
    val text = new String(bytes, "UTF-8")
    val input = Input.VirtualFile(path.toString, text)
    input.parse[Source].get
  }

  def writeTransformed(code: String, p: String) = {
    val path = java.nio.file.Paths.get(p)
    Files.write(path, code.getBytes())
  }

  def attachMonitors(tree: Tree): Tree = {
    this.apply(tree)
  }

  def extractVars(tree: Tree): List[String] = {
    val name = tree match {
      case Term.Select(qual, name) => s"${qual.toString()}.${name.value}"
      case Term.Name(name) => name
      case _ => ""
    }
    (name +: tree.children.flatMap(c => extractVars(c))).filter(_.matches("^[a-zA-Z_$][a-zA-Z_$0-9]*$"))
  }

  def toListStatement(vars: List[String]): String = {
    s"List[Any](${vars.mkString(",")})"
  }
  def attachPredicateMonitor(predicate: Term, prevVars: List[String] = List()): Term = {
    (s"${Constants.MAP_TRANSFORMS(Constants.KEY_PREDICATE)}" +
      s"(${predicate.toString()}, (${toListStatement(extractVars(predicate))}, ${toListStatement(prevVars)}), $id)")
        .parse[Term].get
  }

  def attachDFOMonitor(dfo: Term): Term = {
    id += 1
    val Term.Apply(Term.Select(rddName, Term.Name(dfoName)), args) = dfo
    println(s"found dfo $dfoName called on rdd: $rddName")
    dfoName match {
      case Constants.KEY_JOIN => s"${Constants.MAP_TRANSFORMS(Constants.KEY_JOIN)}($rddName, ${args.mkString(",")}, $id)".parse[Term].get
//      case Constants.KEY_FILTER => s"${Constants.MAP_TRANSFORMS(Constants.KEY_FILTER)}($rddName, ${args.mkString(",")}, $id)".parse[Term].get
      case _ => dfo
    }
  }

  def insertAtEndOfFunction(mainFunc: Defn, statement: Stat): Defn = {
    val Defn.Def(mods, name, tparams, paramss, decltpe, Term.Block(code)) = mainFunc
    Defn.Def(mods, name, tparams, paramss, decltpe, Term.Block(code :+ statement))
  }

  def modifySparkContext(term: Term): Term = {
    Term.New(Init(Type.Name("SparkContextWithDP"), Name(""), List(List(term))))
  }

  def modifyTextFile(term: Term): Term = {
    val Term.Apply(Term.Select(prefix, _), args) = term
    Term.Apply(Term.Select(prefix, Term.Name("textFileProv")), args :+ """_.split(",")""".parse[Term].get)
  }

  override def apply(tree: Tree): Tree = {
    tree match {
      case node @ Term.New(Init(Type.Name("SparkContext"), _, _)) => modifySparkContext(node)
      case node @ Term.Apply(Term.Select(_, Term.Name("textFile")), _) => modifyTextFile(node)
      case node @ Term.Apply(Term.Select(_, name), _) if Constants.MAP_TRANSFORMS.contains(name.value) => super.apply(attachDFOMonitor(node))
      case node @ Defn.Def(_, Term.Name("main"), _, _ , _, _) => super.apply(insertAtEndOfFunction(node, Constants.CONSOLIDATOR.parse[Stat].get))
      case Term.If(predicate, ifBody, elseBody) => super.apply(Term.If(attachPredicateMonitor(predicate), ifBody, elseBody))
      case node @ _ => super.apply(node)
    }
  }
}



