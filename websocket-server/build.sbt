name := """websocket-server"""
organization := "EvolutionGaming"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

val akkaVersion = "2.4.17"

libraryDependencies += filters
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "io.backchat.hookup" % "hookup_2.11" % "0.4.2",
  specs2 % Test
)
