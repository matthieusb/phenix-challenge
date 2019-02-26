package phenix

import java.nio.file.Paths

import org.scalatest.{FlatSpec, Matchers}


class MarshallerSpec extends FlatSpec with Matchers {

  "The Transaction File Marshaller" should "return an empty stream when file is not found" in {
    // PREPARE/EXECUTE
    val transactions = TransactionMarshaller.marshallLines(Paths.get("not/an/existant/path/in/this/project"))

    // ASSERT
    transactions shouldBe empty

  }

  "The Transaction File Marshaller" should "return a stream with the right transactions" in {
    // PREPARE/EXECUTE
    val transactions = TransactionMarshaller.marshallLines(Paths.get("data/input/example/transactions_20170514.data"))

    // ASSERT
    transactions should have size 45906

    transactions should contain (Transaction(1, TransactionMarshaller.CARREFOUR_DATE_FORMAT.parse("20170514T223544+0100"), "2a4b6b81-5aa2-4ad8-8ba9-ae1a006e7d71", 531, 5))
    transactions should contain (Transaction(3818, TransactionMarshaller.CARREFOUR_DATE_FORMAT.parse("20170514T225034+0200"), "72a2876c-bc8b-4f35-8882-8d661fac2606", 989, 4))
    transactions should contain (Transaction(9999, TransactionMarshaller.CARREFOUR_DATE_FORMAT.parse("20170514T111747+0100"), "10f2f3e6-f728-41f3-b079-43b0aa758292", 703, 1))

  }

  "The Product File Marshaller" should "return an empty stream when file is not found" in {
    // PREPARE/EXECUTE
    val products = ProductMarshaller.marshallLines(Paths.get("not/an/existant/path/in/this/project"))

    // ASSERT
    products shouldBe empty
  }

  "The Product File Marshaller" should "return a stream with the right products" in {
    // PREPARE/EXECUTE
    val products = ProductMarshaller.marshallLines(Paths.get("data/input/example/reference_prod-2a4b6b81-5aa2-4ad8-8ba9-ae1a006e7d71_20170514.data"))

    // ASSERT
    products should have size 999

    products should contain (Product(1, 4.7))
    products should contain (Product(500, 34.86))
    products should contain (Product(999, 0.68))
  }
}