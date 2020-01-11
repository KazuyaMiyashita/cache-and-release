import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val commonSettings = Seq(
  scalacOptions ++= "-deprecation" :: "-feature" :: "-Xlint" :: Nil,
  scalacOptions in (Compile, console) ~= {_.filterNot(_ == "-Xlint")},
  scalafmtOnCompile := true
)

lazy val root = (project in file("."))
  .settings(
    name := "redis-practice",
    commonSettings,
    libraryDependencies += scalaTest % Test,
    libraryDependencies += redis,
    libraryDependencies ++= circe,
    libraryDependencies += hikariCP,
    libraryDependencies += mysql,
    parallelExecution in Test := false
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
