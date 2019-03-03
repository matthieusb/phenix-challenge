package phenix.model

import java.time.LocalDate

case class Transaction(transactionId: Int, shopUuid: String, productId: Int, quantity: Int)
case class Product(productId: Int, price: Double)

case class TransactionFileMetaData(date: LocalDate)
case class ProductFileMetaData(shopUuid: String, date: LocalDate)

case class Transactions(transactions: Stream[Transaction], metaData: TransactionFileMetaData)
case class Products(products: Stream[Product], metaData: ProductFileMetaData)
