package scoobie.doobie

import doobie.util.composite.Composite
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.util.fragment.Fragment
import doobie.util.log.LogHandler
import scoobie.ast.{QueryExpression, QueryModify, QuerySelect}

/**
  * Created by jacob.barber on 5/25/16.
  */
case class DoobieSqlInterpreter(genSql: QueryExpression[ScoobieFragmentProducer] => Fragment) {
  def query[B: Composite](ast: QuerySelect[ScoobieFragmentProducer], logHandler: LogHandler): Query0[B] =
    genSql(ast).queryWithLogHandler(logHandler)

  def update(ast: QueryModify[ScoobieFragmentProducer], logHandler: LogHandler): Update0 =
    genSql(ast).updateWithLogHandler(logHandler)
}
