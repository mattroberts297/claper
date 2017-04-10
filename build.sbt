lazy val root = (project in file(".")).settings(
  name := "claper",
  organization := "io.mattroberts",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.11.8",
  scalacOptions := Seq("-deprecation", "-feature", "-unchecked"),
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % "2.3.2",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )
)
