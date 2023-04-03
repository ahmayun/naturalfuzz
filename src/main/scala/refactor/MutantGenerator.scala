package refactor

import scala.meta.{Term, Transformer, Tree, Defn}
import utils.MutationUtils.getRandomElement
import scala.util.Random
import scala.meta.Term.ApplyInfix.unapply
object MutantGenerator extends Transformer {

  val BOOLEAN = 1
  val ARITHMETIC = 2
  val UNKNOWN = 3
  var g_currentNode: Term.ApplyInfix = null
  var g_mutantOp = ""
  var g_mutantObjSuffix = ""
  val g_arithOps = List("+", "-", "*", "/")
  val g_booleanOps = List("==", ">", ">=", "<", "<=", "!=")
  val g_opNames = Map(
    "+" -> "plus",
    "-" -> "minus",
    "*" -> "times",
    "/" -> "div",
    "==" -> "eq",
    ">" -> "gt",
    ">=" -> "gte",
    "<" -> "lt",
    "<=" -> "lte",
    "!=" -> "neq"
  )
  val g_seed = "ahmad35@vt.edu".hashCode
  Random.setSeed(g_seed)
  def getOpType(op: Term.Name): Int = {
    if (g_arithOps.contains(op.toString()))
      ARITHMETIC
    else if (g_booleanOps.contains(op.toString()))
      BOOLEAN
    else
      UNKNOWN
  }
  def getAllBinOpsPositions(tree: Tree): List[Term.ApplyInfix] = {
    tree match {
      case node @ Term.ApplyInfix(_,op,_,_) if List(BOOLEAN, ARITHMETIC).contains(getOpType(op)) =>
        node+:tree.children.map(getAllBinOpsPositions).flatten
      case _ =>
        tree.children.map(getAllBinOpsPositions).flatten
    }
  }

  def setCurrentOp(node: Term.ApplyInfix): (String, String, Int, String, String) = {
    val Some((lhs,oldOp @ Term.Name(oldOpSym), targs, args)) = unapply(node)
    val newOpSym = getOpType(oldOp) match {
      case ARITHMETIC => getRandomElement(g_arithOps.filter(_ != oldOpSym))
      case BOOLEAN => getRandomElement(g_booleanOps.filter(_ != oldOpSym))
    }
    (oldOpSym, g_opNames(oldOpSym), node.pos.startLine, newOpSym, g_opNames(newOpSym))
  }

  def generateMutants(tree: Tree): List[(Tree, String)] = {
    getAllBinOpsPositions(tree).map {
      node =>
        val (oldOpSym, oldOpName, lineNo, newOpSym, newOpName) = setCurrentOp(node)
        g_mutantObjSuffix = s"${lineNo}_${oldOpName}_$newOpName"
        g_currentNode = node
        g_mutantOp = newOpSym
        println(s"changing $oldOpSym to $newOpSym on line $lineNo: $node")
        (apply(tree), g_mutantObjSuffix)
    }
  }

  def areNodesEqual(n1: Tree, n2: Tree): Boolean = {
    (n1.pos.startLine == n2.pos.startLine && n1.pos.endLine == n2.pos.endLine
      && n1.pos.startColumn == n2.pos.startColumn
      && n1.pos.endColumn == n2.pos.endColumn)
  }

  override def apply(tree: Tree): Tree = {
    tree match {
      case Defn.Object(mods, Term.Name(objName), templ) =>
        super.apply(Defn.Object(mods, Term.Name(s"${objName}_$g_mutantObjSuffix"), templ))
      case node @ Term.ApplyInfix(lhs, op, targs, args) if areNodesEqual(node, g_currentNode) =>
        super.apply(Term.ApplyInfix(lhs, Term.Name(g_mutantOp), targs, args))
      case node @ _ =>
        super.apply(node)
    }
  }
}



