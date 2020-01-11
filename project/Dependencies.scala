import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
  lazy val redis = "redis.clients" % "jedis" % "3.2.0"
  lazy val circe = Seq(
    "io.circe" %% "circe-core" % "0.12.3",
    "io.circe" %% "circe-generic" % "0.12.3",
    "io.circe" %% "circe-parser" % "0.12.3"
  )
  lazy val hikariCP = "com.zaxxer" % "HikariCP" % "3.4.1"
  lazy val mysql = "mysql" % "mysql-connector-java" % "8.0.17"
}
