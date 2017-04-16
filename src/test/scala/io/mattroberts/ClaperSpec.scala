package io.mattroberts

import org.scalatest.{MustMatchers, FlatSpec}

class ClaperSpec extends FlatSpec with MustMatchers {
  object NoDefaults {
    case class Args(
      alpha: String,
      bravo: Byte,
      charlie: Short,
      delta: Int,
      echo: Long,
      foxtrot: Float,
      golf: Double,
      hotel: Boolean)

    val args = List(
      "--alpha", "alpha",
      "--bravo", "1",
      "--charlie", "256",
      "--delta", "65536",
      "--echo", "4294967296",
      "--foxtrot", "1.5",
      "--golf", "1.5",
      "--hotel")
  }

  object Defaults {
    case class Args(
      alpha: String = "alpha",
      bravo: Byte = 1,
      charlie: Short = 256,
      delta: Int = 65536,
      echo: Long = 4294967296L,
      foxtrot: Float = 1.5F,
      golf: Double = 1.5,
      hotel: Boolean)

    val args = List.empty[String]
  }

  "Claper" must "parse all args" in {
    import NoDefaults._
    val parsed = Claper[Args].parse(args)
    parsed must be (Right(Args(
      alpha = "alpha",
      bravo = 1,
      charlie = 256,
      delta = 65536,
      echo = 4294967296L,
      foxtrot = 1.5F,
      golf = 1.5,
      hotel = true)))
  }

  it must "use defaults" in {
    import Defaults._
    val parsed = Claper[Args].parse(args)
    parsed must be (Right(Args(
      alpha = "alpha",
      bravo = 1,
      charlie = 256,
      delta = 65536,
      echo = 4294967296L,
      foxtrot = 1.5F,
      golf = 1.5,
      hotel = false)))
  }

  it must "error on missing strings" in {
    import NoDefaults._
    val parsed = Claper[Args].parse(removeNthArg(args, 1))
    parsed must be (Left(ClaperError("Missing argument alpha")))
  }

  it must "error on missing bytes" in {
    import NoDefaults._
    val parsed = Claper[Args].parse(removeNthArg(args, 2))
    parsed must be (Left(ClaperError("Missing argument bravo")))
  }

  it must "error on missing shorts" in {
    import NoDefaults._
    val parsed = Claper[Args].parse(removeNthArg(args, 3))
    parsed must be (Left(ClaperError("Missing argument charlie")))
  }

  it must "error on missing ints" in {
    import NoDefaults._
    val parsed = Claper[Args].parse(removeNthArg(args, 4))
    parsed must be (Left(ClaperError("Missing argument delta")))
  }

  it must "error on missing longs" in {
    import NoDefaults._
    val parsed = Claper[Args].parse(removeNthArg(args, 5))
    parsed must be (Left(ClaperError("Missing argument echo")))
  }

  it must "error on missing floats" in {
    import NoDefaults._
    val parsed = Claper[Args].parse(removeNthArg(args, 6))
    parsed must be (Left(ClaperError("Missing argument foxtrot")))
  }

  it must "error on missing doubles" in {
    import NoDefaults._
    val parsed = Claper[Args].parse(removeNthArg(args, 7))
    parsed must be (Left(ClaperError("Missing argument golf")))
  }

  def removeNthArg(args: Seq[String], n: Int): Seq[String] = {
    val (left, right) = args.splitAt(n * 2)
    left.dropRight(2) ++ right
  }
}
