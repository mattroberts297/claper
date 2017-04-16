package io.mattroberts

import org.scalatest.{MustMatchers, FlatSpec}

class ClaperErrorSpec extends FlatSpec with MustMatchers {
  "ClaperError" must "alias message to msg" in {
    val message = "Test message"
    val error = ClaperError(message)
    error.msg must be (message)
  }
}
