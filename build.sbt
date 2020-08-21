lazy val scala213 = "2.13.1"
lazy val scala212 = "2.12.10"
lazy val scala211 = "2.11.12"
lazy val scala210 = "2.10.7"
lazy val supportedScalaVersions = Seq(scala213, scala212, scala211, scala210)

lazy val dependencies = Def.setting(Seq(
  "com.chuusai" %%% "shapeless" % "2.3.3",
  "org.scalatest" %%% "scalatest" % "3.2.1" % "test"
))

lazy val dependenciesForScala210 = Def.setting(Seq(
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
))

lazy val sharedSettings = Seq(
  name := "claper",
  organization := "io.mattroberts",
  scalacOptions := Seq("-deprecation", "-feature", "-unchecked"),
  libraryDependencies ++= (scalaBinaryVersion.value match {
    case "2.10" => dependencies.value ++ dependenciesForScala210.value
    case _      => dependencies.value
  })
)

lazy val jvmSettings = Seq(
  scalaVersion := scala213,
  crossScalaVersions := supportedScalaVersions
)

lazy val nativeSettings = Seq(
  scalaVersion := scala211,
  crossScalaVersions := Seq(scala211),
  nativeLinkStubs := true
)

lazy val publishSettings = Seq(
  credentials ++= Seq(
    Credentials(
      "Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      "mattroberts297",
      env("SONATYPE_PASSWORD")
    ),
    Credentials(
      "GnuPG Key ID",
      "gpg",
      "B89EBE31B8541C9AB694A7063926C2AF62D1F8D5",
      env("PGP_PASSPHRASE")
    )
  ),

  publishTo := sonatypePublishToBundle.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,

  pomIncludeRepository := {
    _: sbt.MavenRepository => false
  },

  licenses := Seq("MIT" -> url("https://github.com/mattroberts297/claper/blob/master/LICENSE")),
  homepage := Some(url("https://github.com/mattroberts297/claper")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/mattroberts297/claper"),
      "scm:git@github.com:mattroberts297/claper.git"
    )
  ),
  developers := List(
    Developer(
      id    = "mattroberts297",
      name  = "Matt Roberts",
      email = "mail@mattroberts.io",
      url   = url("https://mattroberts.io")
    )
  )
) 

lazy val claper = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(sharedSettings)
  .jvmSettings(jvmSettings)
  .nativeSettings(nativeSettings)

def env(name: String): String = System.getenv().get(name)

lazy val root = project
  .in(file("."))
  .aggregate(claper.jvm, claper.native)
  .settings(crossScalaVersions := Nil)