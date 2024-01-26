package forex.domain

case class OneFrameRatesResponse (
                                   from: String,
                                   to: String,
                                   bid: BigDecimal,
                                   ask: BigDecimal,
                                   price: BigDecimal,
                                   time_stamp: String
                                 )
