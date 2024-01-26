package forex.domain

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, OffsetDateTime, ZoneOffset}
import scala.util.Try

case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {
  def now: Timestamp =
    Timestamp(OffsetDateTime.now)

  def fromStringDate(date: String, format: String): Option[Timestamp] = {
    Try{
      val dateFormatter = DateTimeFormatter.ofPattern(format)
      val localTime = LocalDateTime.parse(date, dateFormatter)
      Timestamp(OffsetDateTime.of(localTime, ZoneOffset.UTC))
    }.toOption
  }
}
