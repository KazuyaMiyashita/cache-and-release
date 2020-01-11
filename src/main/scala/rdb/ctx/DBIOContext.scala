package rdb.ctx

import java.sql.Connection
import scala.util.Try

class DBIOContext(pool: DBPool) extends IOContext[Try, Connection] {

  override def init: Connection = {
    val con: Connection = pool.getConnection()
    con.setAutoCommit(false)
    con
  }
  override def transaction[T](execution: Connection => Try[T]): Try[T] = ???
  override def run(): Unit                                             = ???

}
