lazy val root = (project in file(".")).settings(
  name := "claper",
  organization := "io.mattroberts",
  version := "0.1.0",
  scalaVersion := "2.12.1",
  scalacOptions := Seq("-deprecation", "-feature", "-unchecked"),
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % "2.3.2",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  ),

  pgpPassphrase := Option(env("PGP_PASSPHRASE")).map(_.toCharArray),
  pgpSecretRing := file("local.secret.asc"),
  pgpPublicRing := file("local.public.asc"),

  credentials += Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    "mattroberts297",
    env("SONATYPE_PASSWORD")
  ),

  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },

  makePomConfiguration ~= {
    _.copy(configurations = Some(Seq(Compile, Runtime, Optional)))
  },
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

def env(name: String): String = System.getenv().get(name)
