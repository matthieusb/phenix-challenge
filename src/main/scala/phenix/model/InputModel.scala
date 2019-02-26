package phenix.model

import java.util.Date

case class Transaction(transactionId: Int, date: Date, shopUuid: String, productId: Int, quantity: Int)
case class Product(productId: Int, price: Double)

