package rdb

import entity.{UserId, User}
import java.util.UUID
import rdb.ctx.DBPool
import rdb.query._

class UserDao()(implicit pool: DBPool) {

  def fetchById(id: UserId): Option[User] = {
    select(
      "select id, name, age from users where id = \"%s\"".format(id.value.toString),
      row =>
        User(
          id = UserId(UUID.fromString(row.getString("id"))),
          name = row.getString("name"),
          age = row.getInt("age")
        )
    )
  }

}
