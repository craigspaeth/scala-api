name := "Hellow World"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.1.0",
  "org.sangria-graphql" %% "sangria-spray-json" % "1.0.0",
  "com.typesafe.akka" %% "akka-http" % "10.0.1",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % Test
)
