package rdb

import entity.{UserId, User}
import java.util.UUID
import rdb.ctx.DBPool
import java.sql.{Connection, Statement, ResultSet}
import java.sql.SQLException

class UserDao(pool: DBPool) {

  def fetchById(id: UserId): Option[User] = {
    def toEntity(id: String, name: String, age: Int): User = User(
      id = UserId(UUID.fromString(id)),
      name = name,
      age = age
    )

    val db: Connection = pool.getConnection()
    db.setAutoCommit(false)
    try {
      val stmt: Statement = db.createStatement()
      val query: String   = "select id, name, age from users where id = \"%s\"".format(id.value.toString)
      db.commit()
      val resultSet: ResultSet = stmt.executeQuery(query)
      val users: List[User] = Iterator
        .continually(resultSet)
        .takeWhile(_.next())
        .map(
          row =>
            toEntity(
              row.getString("id"),
              row.getString("name"),
              row.getInt("age")
            )
        )
        .toList
      users.headOption
    } catch {
      case e: SQLException => {
        db.rollback()
        e.printStackTrace()
        throw e
      }
    } finally {
      db.close()
    }
  }

}
