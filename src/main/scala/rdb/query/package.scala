package rdb

import java.sql.ResultSet
import java.sql.SQLException
import rdb.ctx.DBPool

package object query {

  def select[T](query: String, mapping: ResultSet => T)(implicit pool: DBPool): Option[T] = {
    val conn = pool.getConnection()
    conn.setAutoCommit(false)
    try {
      val stmt      = conn.createStatement()
      val resultSet = stmt.executeQuery(query)
      Iterator.continually(resultSet).takeWhile(_.next()).map(mapping).toList.headOption
    } catch {
      case e: SQLException => {
        conn.rollback()
        e.printStackTrace()
        throw e
      }
    } finally {
      conn.close()
    }
  }

}
