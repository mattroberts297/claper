import org.scalatest.{MustMatchers, FlatSpec}

class ParserSpec extends FlatSpec with MustMatchers {
  "A Parser" must "parse SimpleArguments" in {
    val args = List("a", "1", "true")
    val parsed = Parser[SimpleArguments].parse(args)
    parsed must be (SimpleArguments("a", 1, true))
  }
}
