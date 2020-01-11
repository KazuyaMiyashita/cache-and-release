package rdb

import org.scalatest._
import rdb.ctx.{DBPool, DefaultDBPool}
import entity.{UserId, User}
import java.util.UUID

class UserDaoSpec extends FlatSpec with Matchers with BeforeAndAfter {

  val pool: DBPool = DefaultDBPool

  def clean(): Unit = ()

  before(clean())
  after(clean())

  "UserDao" should "fetchById" in {

    val userDao: UserDao = new UserDao(pool)
    val userId           = UserId(UUID.fromString("b73976f4-36e8-4a56-976a-bf75030f23eb"))
    userDao.fetchById(userId) shouldEqual Some(
      User(
        id = UserId(UUID.fromString("b73976f4-36e8-4a56-976a-bf75030f23eb")),
        name = "Alice",
        age = 18
      )
    )

  }

}
