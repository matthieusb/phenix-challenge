package phenix.model

import java.util.Date

case class Transaction(transactionId: Int, date: Date, shopUuid: String, productId: Int, quantity: Int)
case class Product(productId: Int, price: Double)

case class TransactionFileMetaData(date: Date)
case class ProductFileMetaData(shopUuid: String, date: Date)

case class Transactions(transactions: Stream[Transaction], metaData: TransactionFileMetaData)
case class Products(products: Stream[Product], metaData: ProductFileMetaData)
