package refactor

import java.nio.file.Files
import scala.meta.{Defn, Input, Source, Stat, Term, Transformer, Tree, XtensionParseInputLike}

object MonitorAttacher extends Transformer {

  val monitorClass = "_root_.monitoring.Monitors"
  val mapTransforms = Map(
    "tFilter" -> "monitorFilter",
    "tJoin" -> "monitorJoin",
    "tPredicate" -> "monitorPredicate",
    "tFinalize" -> "finalizeProvenance()"
  ).mapValues(s => s"$monitorClass.$s")

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
    s"${mapTransforms("tPredicate")}(${predicate.toString()}, (${toListStatement(extractVars(predicate))}, ${toListStatement(prevVars)}), 1)".parse[Term].get
  }

  def attachDFOMonitor(dfo: Term): Term = {
    println("found dfo")
    dfo
  }

  def insertAtEndOfFunction(mainFunc: Defn, statement: Stat): Defn = {
    val Defn.Def(mods, name, tparams, paramss, decltpe, Term.Block(code)) = mainFunc
    Defn.Def(mods, name, tparams, paramss, decltpe, Term.Block(code :+ statement))
  }

  override def apply(tree: Tree): Tree = {
    super.apply(tree match {
      case Term.If(predicate, ifBody, elseBody) => Term.If(attachPredicateMonitor(predicate), ifBody, elseBody)
      case node @ Term.Apply(Term.Select(_, name), _) if mapTransforms.contains(name.value) => attachDFOMonitor(node)
      case node @ Defn.Def(_, Term.Name("main"), _, _ , _, _) => insertAtEndOfFunction(node, mapTransforms("tFinalize").parse[Stat].get)
      case node @ _ => node
    })
  }
}



