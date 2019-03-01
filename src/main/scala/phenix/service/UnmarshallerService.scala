package phenix.service

import phenix.model.{ProductSale, ProductTurnover}


trait UnmarshallerService[T] extends FileProducer {
  val OUTPUT_HORIZONTAL_SEPARATOR: String = """\|"""

  // TODO
}

object ProductSaleUnmarshallerService extends UnmarshallerService[ProductSale] {
  // TODO
}

object ProductTurnoverUnmarshallerService extends UnmarshallerService[ProductTurnover] {
  // TODO
}