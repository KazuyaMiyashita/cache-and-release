package entity

import java.util.UUID

case class UserId(value: UUID)
case class User(id: UserId, name: String, age: Int)
