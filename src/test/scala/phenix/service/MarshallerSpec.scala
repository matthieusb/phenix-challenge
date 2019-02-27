package phenix.service

import java.nio.file.Paths

import org.scalatest.{FlatSpec, Matchers}
import phenix.model.{Product, Products, Transaction, Transactions}

import scala.util.{Failure, Try}


class MarshallerSpec extends FlatSpec with Matchers {

  "The Transaction File Marshaller" should "return an error when file is not found" in {
    // PREPARE/EXECUTE
    val transactions: Try[Transactions] = TransactionMarshaller.marshallLines(Paths.get("not/an/existant/path/in/this/project"))

    // ASSERT
    transactions.isFailure shouldBe true
  }

  "The Transaction File Marshaller" should "return a stream with the right transactions and correct file metadata" in {
    // PREPARE/EXECUTE
    val transactions: Try[Transactions] = TransactionMarshaller.marshallLines(Paths.get("data/input/example/transactions_20170514.data"))

    // ASSERT
    transactions.isSuccess shouldBe true

    transactions.get.transactions should have size 45906

    transactions.get.transactions should contain (Transaction(1, TransactionMarshaller.CARREFOUR_TRANSACTION_DATE_FORMAT.parse("20170514T223544+0100"), "2a4b6b81-5aa2-4ad8-8ba9-ae1a006e7d71", 531, 5))
    transactions.get.transactions should contain (Transaction(3818, TransactionMarshaller.CARREFOUR_TRANSACTION_DATE_FORMAT.parse("20170514T225034+0200"), "72a2876c-bc8b-4f35-8882-8d661fac2606", 989, 4))
    transactions.get.transactions should contain (Transaction(9999, TransactionMarshaller.CARREFOUR_TRANSACTION_DATE_FORMAT.parse("20170514T111747+0100"), "10f2f3e6-f728-41f3-b079-43b0aa758292", 703, 1))


    transactions.get.metaData.date shouldBe TransactionMarshaller.CARREFOUR_FILENAME_DATE_FORMAT.parse("20170514")
  }

  "The Product File Marshaller" should "return an error when file is not found" in {
    // PREPARE/EXECUTE
    val products: Try[Products] = ProductMarshaller.marshallLines(Paths.get("not/an/existant/path/in/this/project"))

    // ASSERT
    products.isFailure shouldBe true
  }

  "The Product File Marshaller" should "return a stream with the right products and correct file metadata" in {
    // PREPARE/EXECUTE
    val products: Try[Products] = ProductMarshaller.marshallLines(Paths.get("data/input/example/reference_prod-2a4b6b81-5aa2-4ad8-8ba9-ae1a006e7d71_20170514.data"))

    // ASSERT
    products.isSuccess shouldBe true

    products.get.products should have size 999
    products.get.products should contain (Product(1, 4.7))
    products.get.products should contain (Product(500, 34.86))
    products.get.products should contain (Product(999, 0.68))

    products.get.metaData.shopUuid shouldBe "2a4b6b81-5aa2-4ad8-8ba9-ae1a006e7d71"
    products.get.metaData.date shouldBe ProductMarshaller.CARREFOUR_FILENAME_DATE_FORMAT.parse("20170514")
  }
}