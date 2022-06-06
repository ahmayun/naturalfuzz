package utils

import symbolicexecution.SymExResult

object RIGUtils {
  def createFilterQueries(pathExpressions: SymExResult): FilterQueries = {
    new FilterQueries(pathExpressions)
  }

}
