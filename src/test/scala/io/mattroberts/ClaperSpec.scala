package io.mattroberts

import org.scalatest.{MustMatchers, FlatSpec}

class ClaperSpec extends FlatSpec with MustMatchers {
  "Claper" must "parse all args" in {
    case class Args(alpha: String, beta: Int, charlie: Boolean)
    val args = List("--alpha", "alpha", "--beta", "1", "--charlie")
    val parsed = Claper[Args].parse(args)
    parsed must be (Right(Args("alpha", 1, true)))
  }

  it must "use defaults" in {
    case class Args(alpha: String = "alpha", beta: Int = 1, charlie: Boolean)
    val args = List.empty[String]
    val parsed = Claper[Args].parse(args)
    parsed must be (Right(Args("alpha", 1, false)))
  }

  it must "error on missing strings" in {
    case class Args(alpha: String, beta: Int, charlie: Boolean)
    val args = List("--beta", "1", "--charlie")
    val parsed = Claper[Args].parse(args)
    parsed must be (Left(ClaperError("Missing argument alpha")))
  }

  it must "error on missing ints" in {
    case class Args(alpha: String, beta: Int, charlie: Boolean)
    val args = List("--alpha", "alpha", "--charlie")
    val parsed = Claper[Args].parse(args)
    parsed must be (Left(ClaperError("Missing argument beta")))
  }
}
