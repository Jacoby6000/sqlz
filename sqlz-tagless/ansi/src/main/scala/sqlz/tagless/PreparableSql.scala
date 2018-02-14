package sqlz.algebra

import java.sql.{PreparedStatement, Connection}

sealed trait PreparableSql {
  def unpreparedStatement: Connection => PreparedStatement = conn =>
    conn.prepareStatement(sqlString)

  def preparedStatement: Connection => PreparedStatement = conn => gatherPreparations.foldLeft(unpreparedStatement(conn))((stmt, f) => f(stmt))

  def gatherPreparations: List[PreparedStatement => PreparedStatement] = {
    def gatherWithIndex(preparableSql: PreparableSql): List[(PreparedStatement, Int) => PreparedStatement] =
      this match {
        case Prepare(f) => List(f)
        case Combine(l, r) => gatherWithIndex(l) ++ gatherWithIndex(r)
        case QueryString(_) => List()
      }

    gatherWithIndex(this).zipWithIndex.map { case (f, idx) => f((_: PreparedStatement), idx) }
  }

  def sqlString: String =
    this match {
      case Prepare(_) => "?"
      case Combine(l, r) => l.sqlString + r.sqlString
      case QueryString(s) => s
    }
}

case class Prepare(f: (PreparedStatement, Int) => PreparedStatement) extends PreparableSql
case class Combine(l: PreparableSql, r: PreparableSql) extends PreparableSql
case class QueryString(s: String) extends PreparableSql

