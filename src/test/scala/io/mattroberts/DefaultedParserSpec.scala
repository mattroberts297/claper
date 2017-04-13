package io.mattroberts

import org.scalatest.{MustMatchers, FlatSpec}

class DefaultedParserSpec extends FlatSpec with MustMatchers {
  "A DefaultedParser" must "parse SimpleArguments" in {
    val args = List("--beta", "1", "--charlie")
    val parsed = DefaultedParser[SimpleArguments].parse(args)
    parsed must be (SimpleArguments("alpha", 1, true))
  }
}
