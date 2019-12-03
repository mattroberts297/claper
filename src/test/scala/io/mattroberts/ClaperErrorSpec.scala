package io.mattroberts

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class ClaperErrorSpec extends AnyFlatSpec with Matchers {
  "ClaperError" must "alias message to msg" in {
    val message = "Test message"
    val error = ClaperError(message)
    error.msg must be (message)
  }
}
