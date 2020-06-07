package cache

import org.scalatest._
import redis.clients.jedis.{Jedis, JedisPool}

class RedisCacheSpec extends FlatSpec with Matchers with BeforeAndAfter {

  val pool: JedisPool = new JedisPool("127.0.0.1", 16379)

  def clean(): Unit = {
    val j: Jedis = pool.getResource()
    j.flushDB()
    j.close()
  }

  before(clean())
  after(clean())

  "RedisCache" should "set and get" in {
    val redis = new DefaultRedisCache(pool)
    redis.set("key1", "value1")
    redis.set("key2", "value2")
    redis.get("key1") shouldEqual Some("value1")
    redis.get("key2") shouldEqual Some("value2")
  }

  "RedisCache" should "get returns None" in {
    val redis = new DefaultRedisCache(pool)
    redis.set("key1", "value1")
    redis.get("key1") shouldEqual Some("value1")
    redis.get("key2") shouldEqual None
  }

  "RedisCache" should "set and mget" in {
    val redis = new DefaultRedisCache(pool)
    redis.set("key1", "value1")
    redis.set("key2", "value2")
    redis.mget(Set("key1", "key2", "nothing")) shouldEqual Map(
      "key1" -> "value1",
      "key2" -> "value2"
    )
  }

  "RedisCache" should "mset and mget" in {
    val redis = new DefaultRedisCache(pool)
    redis.mset(
      Map(
        "key1" -> "value1",
        "key2" -> "value2"
      )
    )
    redis.mget(Set("key1", "key2", "nothing")) shouldEqual Map(
      "key1" -> "value1",
      "key2" -> "value2"
    )
  }

  "RedisCache" should "delete" in {
    val redis = new DefaultRedisCache(pool)
    redis.set("key1", "value1")
    redis.set("key2", "value2")
    redis.delete("key2")
    redis.get("key1") shouldEqual Some("value1")
    redis.get("key2") shouldEqual None
  }

  "RedisCache" should "flush" in {
    val redis = new DefaultRedisCache(pool)
    redis.set("key1", "value1")
    redis.set("key2", "value2")
    redis.flush()
    redis.get("key1") shouldEqual None
    redis.get("key2") shouldEqual None
  }

  "RedisCache" should "mapValue" in {
    import io.circe._
    import io.circe.syntax._
    import io.circe.generic.semiauto._
    case class User(name: String, age: Int)
    implicit val userDecoder: Decoder[User] = deriveDecoder
    implicit val userEncoder: Encoder[User] = deriveEncoder
    val decoder: String => User =
      parser.parse(_).flatMap(_.as[User]).getOrElse(throw new RuntimeException("decode error"))
    val encoder: User => String = _.asJson.noSpaces

    val redis = new DefaultRedisCache(pool).mapValue(decoder, encoder)

    redis.set("key", User("Alice", 24))
    redis.get("key") shouldEqual Some(User("Alice", 24))
  }

  "RedisCache" should "mapKey" in {
    case class UserId(value: String)
    val redis = new DefaultRedisCache(pool).mapKey(UserId(_), (_: UserId).value)
    redis.set(UserId("key"), "value")
    redis.get(UserId("key")) shouldEqual Some("value")
  }

  "RedisCache" should "mapKey and mapValue" in {
    import io.circe._
    import io.circe.syntax._
    import io.circe.generic.semiauto._
    case class User(name: String, age: Int)
    implicit val userDecoder: Decoder[User] = deriveDecoder
    implicit val userEncoder: Encoder[User] = deriveEncoder
    val decoder: String => User =
      parser.parse(_).flatMap(_.as[User]).getOrElse(throw new RuntimeException("decode error"))
    val encoder: User => String = _.asJson.noSpaces

    case class UserId(value: String)

    val redis = new DefaultRedisCache(pool)
      .mapValue(decoder, encoder)
      .mapKey(UserId(_), (_: UserId).value)

    redis.set(UserId("key"), User("Alice", 24))
    redis.get(UserId("key")) shouldEqual Some(User("Alice", 24))
  }

  "RedisCache" should "withHash (1)" in {
    val redis  = new DefaultRedisCache(pool)
    val cacheA = redis.withHash("a")
    val cacheB = redis.withHash("b")
    cacheA.set("key", "valueA")
    cacheB.set("key", "valueB")
    cacheA.get("key") shouldEqual Some("valueA")
    cacheB.get("key") shouldEqual Some("valueB")
  }

  "RedisCache" should "withHash (2)" in {
    val redis     = new DefaultRedisCache(pool)
    val userCache = redis.withHash("user")
    userCache.set("user1", "value1")
    userCache.set("user2", "value2")
    userCache.get("user1") shouldEqual Some("value1")
    userCache.get("user2") shouldEqual Some("value2")
    userCache.get("nothing") shouldEqual None
    userCache.mget(Set("user1", "user2", "nothing")) shouldEqual Map(
      "user1" -> "value1",
      "user2" -> "value2"
    )
    userCache.delete("user1")
    userCache.get("user1") shouldEqual None
    redis.set("foo", "bar")
    redis.get("foo") shouldEqual Some("bar")
    redis.get("user2") shouldEqual None
    userCache.flush()
    userCache.get("user2") shouldEqual None
    redis.get("foo") shouldEqual Some("bar")
  }

  "RedisCache" should "withHash (getAll)" in {
    val redis = new DefaultRedisCache(pool)

    redis.set("default1", "value1")
    redis.set("default2", "value2")
    redis.getAll() shouldEqual Map(
      "default1" -> "value1",
      "default2" -> "value2"
    )

    val userCache = redis.withHash("user")
    userCache.set("user1", "value1")
    userCache.set("user2", "value2")
    userCache.getAll() shouldEqual Map(
      "user1" -> "value1",
      "user2" -> "value2"
    )

    case class Image(title: String, url: String)
    case class ImageId(value: Int)

    import io.circe._
    import io.circe.syntax._
    import io.circe.generic.semiauto._

    val imageDecoder: String => Image =
      parser.parse(_).flatMap(_.as[Image](deriveDecoder)).getOrElse(throw new RuntimeException("decode error"))
    val imageEncoder: Image => String = _.asJson(deriveEncoder).noSpaces

    val imageCache = redis
      .withHash("image")
      .mapValue(imageDecoder, imageEncoder)
      .mapKey(k => ImageId(k.toInt), (_: ImageId).value.toString)
    imageCache.mset(
      Map(
        ImageId(1) -> Image("image1", "http://example.com/images/001.jpg"),
        ImageId(2) -> Image("image2", "http://example.com/images/002.jpg"),
        ImageId(3) -> Image("image3", "http://example.com/images/003.jpg")
      )
    )
    imageCache.getAll() shouldEqual Map(
      ImageId(1) -> Image("image1", "http://example.com/images/001.jpg"),
      ImageId(2) -> Image("image2", "http://example.com/images/002.jpg"),
      ImageId(3) -> Image("image3", "http://example.com/images/003.jpg")
    )

    redis.getAll() shouldEqual Map(
      "default1" -> "value1",
      "default2" -> "value2"
    )
  }

  "RedisCache" should "cache something" in {
    import io.circe._
    import io.circe.syntax._
    import io.circe.generic.semiauto._
    case class User(name: String, age: Int)
    case class UserId(value: Int)
    val userDecoder: String => User =
      parser.parse(_).flatMap(_.as[User](deriveDecoder)).getOrElse(throw new RuntimeException("decode error"))
    val userEncoder: User => String = _.asJson(deriveEncoder).noSpaces

    case class Image(title: String, url: String)
    case class ImageId(value: Int)

    val imageDecoder: String => Image =
      parser.parse(_).flatMap(_.as[Image](deriveDecoder)).getOrElse(throw new RuntimeException("decode error"))
    val imageEncoder: Image => String = _.asJson(deriveEncoder).noSpaces

    val redis = new DefaultRedisCache(pool)
    val userCache = redis
      .withHash("users")
      .mapValue(userDecoder, userEncoder)
      .mapKey(k => UserId(k.toInt), (_: UserId).value.toString)
    val imageCache = redis
      .withHash("image")
      .mapValue(imageDecoder, imageEncoder)
      .mapKey(k => ImageId(k.toInt), (_: ImageId).value.toString)

    userCache.mset(
      Map(
        UserId(1) -> User("Alice", 21),
        UserId(2) -> User("Bob", 22),
        UserId(3) -> User("Chris", 23)
      )
    )
    imageCache.mset(
      Map(
        ImageId(1) -> Image("image1", "http://example.com/images/001.jpg"),
        ImageId(2) -> Image("image2", "http://example.com/images/002.jpg"),
        ImageId(3) -> Image("image3", "http://example.com/images/003.jpg")
      )
    )
    userCache.get(UserId(1)) shouldEqual Some(User("Alice", 21))
    userCache.get(UserId(2)) shouldEqual Some(User("Bob", 22))
    userCache.get(UserId(3)) shouldEqual Some(User("Chris", 23))
    userCache.get(UserId(4)) shouldEqual None
    imageCache.get(ImageId(1)) shouldEqual Some(Image("image1", "http://example.com/images/001.jpg"))
    imageCache.get(ImageId(2)) shouldEqual Some(Image("image2", "http://example.com/images/002.jpg"))
    imageCache.get(ImageId(3)) shouldEqual Some(Image("image3", "http://example.com/images/003.jpg"))
    imageCache.get(ImageId(4)) shouldEqual None

    userCache.mget(Set(UserId(1), UserId(2), UserId(3), UserId(4))) shouldEqual Map(
      UserId(1) -> User("Alice", 21),
      UserId(2) -> User("Bob", 22),
      UserId(3) -> User("Chris", 23)
    )
    imageCache.mget(Set(ImageId(1), ImageId(2), ImageId(3), ImageId(4))) shouldEqual Map(
      ImageId(1) -> Image("image1", "http://example.com/images/001.jpg"),
      ImageId(2) -> Image("image2", "http://example.com/images/002.jpg"),
      ImageId(3) -> Image("image3", "http://example.com/images/003.jpg")
    )

    userCache.delete(UserId(1))
    userCache.get(UserId(1)) shouldEqual None
    imageCache.get(ImageId(1)) shouldEqual Some(Image("image1", "http://example.com/images/001.jpg"))

    userCache.flush()
    userCache.get(UserId(1)) shouldEqual None
    userCache.get(UserId(2)) shouldEqual None
    userCache.get(UserId(3)) shouldEqual None
    imageCache.get(ImageId(1)) shouldEqual Some(Image("image1", "http://example.com/images/001.jpg"))
    imageCache.get(ImageId(2)) shouldEqual Some(Image("image2", "http://example.com/images/002.jpg"))
    imageCache.get(ImageId(3)) shouldEqual Some(Image("image3", "http://example.com/images/003.jpg"))

    redis.flush()
    userCache.get(UserId(1)) shouldEqual None
    userCache.get(UserId(2)) shouldEqual None
    userCache.get(UserId(3)) shouldEqual None
    imageCache.get(ImageId(1)) shouldEqual None
    imageCache.get(ImageId(2)) shouldEqual None
    imageCache.get(ImageId(3)) shouldEqual None
  }

}
