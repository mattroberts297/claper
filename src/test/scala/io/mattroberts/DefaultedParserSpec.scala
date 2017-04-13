package io.mattroberts

import org.scalatest.{MustMatchers, FlatSpec}

class DefaultedParserSpec extends FlatSpec with MustMatchers {
  "A DefaultedParser" must "parse SimpleArguments" in {
    import shapeless._
    val args = List("--beta", "1", "--charlie")
    val default = Default[SimpleArguments]
    val parsed = DefaultedParser[SimpleArguments].parse(args)
    parsed must be (SimpleArguments("alpha", 1, true))
  }
}
