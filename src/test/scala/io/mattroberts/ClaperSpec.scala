package io.mattroberts

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class ClaperSpec extends AnyFlatSpec with Matchers {
  object NoDefaults {
    case class Args(
      alpha: String,
      bravo: Byte,
      charlie: Short,
      delta: Int,
      echo: Long,
      foxtrot: Float,
      golf: Double,
      hotel: Boolean,
      india: Char)

    val args = List(
      "--alpha", "alpha",
      "--bravo", "1",
      "--charlie", "256",
      "--delta", "65536",
      "--echo", "4294967296",
      "--foxtrot", "1.5",
      "--golf", "1.5",
      "--hotel",
      "--india", "a")
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
      hotel: Boolean = false,
      india: Char = 'a')

    val args = List.empty[String]
  }

  object Options {
    case class Args(
      alpha: Option[String],
      bravo: Option[Byte],
      charlie: Option[Short],
      delta: Option[Int],
      echo: Option[Long],
      foxtrot: Option[Float],
      golf: Option[Double],
      hotel: Option[Boolean],
      india: Option[Char]
    )

    val parsedArgs = Args(
      alpha = Some("alpha"),
      bravo = Some(1),
      charlie = Some(256),
      delta = Some(65536),
      echo = Some(4294967296L),
      foxtrot = Some(1.5f),
      golf = Some(1.5d),
      hotel = Some(true),
      india = Some('a')
    )

    val args = List(
      "--alpha", "alpha",
      "--bravo", "1",
      "--charlie", "256",
      "--delta", "65536",
      "--echo", "4294967296",
      "--foxtrot", "1.5",
      "--golf", "1.5",
      "--hotel",
      "--india", "a")
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
      hotel = true,
      india = 'a')))
  }

  it must "use defaults" in {
    import Defaults._
    val parsed = Claper[Args].parse(args)
    parsed must be (Right(Args()))
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

  it must "error on missing chars" in {
    import NoDefaults._
    val parsed = Claper[Args].parse(removeNthArg(args, 8))
    parsed must be (Left(ClaperError("Missing argument india")))
  }
  it must "ignore missing optional strings" in {
    import Options._
    val parsed = Claper[Args].parse(removeNthArg(args, 1))
    parsed must be (Right(parsedArgs.copy(alpha = None)))
  }

  def removeNthArg(args: Seq[String], n: Int): Seq[String] = {
    val (left, right) = args.splitAt(n * 2)
    left.dropRight(2) ++ right
  }
}
