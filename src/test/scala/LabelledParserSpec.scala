import org.scalatest.{MustMatchers, FlatSpec}

class LabelledParserSpec extends FlatSpec with MustMatchers {
  "A LabelledParser" must "parse SimpleArguments" in {
    val args = List("--alpha", "a", "--beta", "1", "--charlie", "true")
    val parsed = LabelledParser[SimpleArguments].parse(args)
    parsed must be (SimpleArguments("a", 1, true))
  }
}
