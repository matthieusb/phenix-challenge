package phenix

import org.scalatest._

class MainSpec extends FlatSpec with Matchers {
  "The Main object" should "say hello" in {
    Main.greeting shouldEqual "hello"
  }
}
