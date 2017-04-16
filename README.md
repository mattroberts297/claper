# CLAPer

[![Build status](https://travis-ci.org/mattroberts297/claper.svg?branch=master)](https://travis-ci.org/mattroberts297/claper)
[![Coverage status](https://coveralls.io/repos/github/mattroberts297/claper/badge.svg?branch=master)](https://coveralls.io/github/mattroberts297/claper?branch=master)
[![Maven central](https://img.shields.io/maven-central/v/io.mattroberts/claper_2.12.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.mattroberts%22%20a%3A%22claper_2.12%22)

A Command Line Argument Parser without the boiler plate.

## Getting Started

Add the library as a dependency in your project's `build.sbt` file:

```scala
scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "io.mattroberts" %% "claper" % "0.3.0"
)
```

Then use it to parse command line arguments:

```scala
import io.mattroberts.Claper
case class Args(alpha: String, beta: Int, charlie: Boolean)
val args = List("--alpha", "alpha", "--beta", "1", "--charlie")
val parsed = Claper[Args].parse(args)
println(parsed) // Right(Args("alpha", 1, true))
```

## Usage

See [ClaperSpec](src/test/scala/io/mattroberts/ClaperSpec.scala) for full usage.

## Features

- Support for case classes (products)
- Support for default values
- Support for Linux style arguments

## Future Features

In the future I might:

- Add coproduct support
- Add short Linux style arguments
