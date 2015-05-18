
name := """scraper"""

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test",
  "org.jsoup" % "jsoup" % "1.8.1",
  "com.typesafe.akka" % "akka-stream-testkit-experimental_2.11" % "1.0-RC2" % "test",
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-RC2"
)