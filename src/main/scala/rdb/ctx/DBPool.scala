package rdb.ctx

import java.sql.Connection
import com.zaxxer.hikari._

trait DBPool {
  def getConnection(): Connection
}

object DefaultDBPool extends DBPool {

  private lazy val dataSource: HikariDataSource = {
    val _ds = new HikariDataSource()
    _ds.setDriverClassName("com.mysql.cj.jdbc.Driver")
    _ds.setJdbcUrl("jdbc:mysql://127.0.0.1:13306/db")
    _ds.setUsername("root")
    _ds
  }

  override def getConnection(): Connection = {
    dataSource.getConnection
  }

}
