ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "untitled"
  )

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "requests" % "0.6.5",
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
  "org.json4s" %% "json4s-native" % "4.1.0-M5",
  "org.scalaj" %% "scalaj-http" % "2.4.2"
)
