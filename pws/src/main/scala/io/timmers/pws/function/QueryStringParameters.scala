package io.timmers.pws.function

import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.{ Instant, ZoneOffset }

import scala.util.Try

import io.timmers.pws.core.Measurement

import zio.json.{ DeriveJsonDecoder, JsonDecoder, jsonField }

// https://support.weather.com/s/article/PWS-Upload-Protocol?language=en_US
case class QueryStringParameters(
  // ID as registered by wunderground.com (required)
  @jsonField("ID") id: String,
  // Station Key registered with this PWS ID, case sensitive (required)
  @jsonField("PASSWORD") password: String,
  // YYYY-MM-DD HH:MM:SS in UTC or "now" (required)
  dateutc: String,
  @jsonField("UV") uv: Int,
  absbaromin: Double,
  // "updateraw"
  action: String,
  // barometric pressure inches
  baromin: Double,
  // rain inches so far today in local time
  dailyrainin: Double,
  // F outdoor dewpoint F
  dewptf: Double,
  // % outdoor humidity 0-100%
  humidity: Int,
  // % indoor humidity 0-100
  indoorhumidity: Int,
  // F indoor temperature F
  indoortempf: Double,
  // rain inches over the past month
  monthlyrainin: Double,
  // rain inches over the past hour
  rainin: Double,
  realtime: Int,
  rtfreq: Int,
  softwaretype: String,
  // W/m^2
  solarradiation: Double,
  // F outdoor temperature
  tempf: Double,
  // rain inches over the past week
  weeklyrainin: Double,
  windchillf: Double,
  // 0-360 instantaneous wind direction
  winddir: Int,
  // mph current wind gust, using software specific time period
  windgustmph: Double,
  // mph instantaneous wind speed
  windspeedmph: Double
) {
  private val DateTimeFormat = new DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd HH:mm:ss")
    .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
    .toFormatter()
    .withZone(ZoneOffset.UTC);

  def toMeasurement: Either[String, Measurement] = for {
    timestamp                  <- timestamp(dateutc)
    absoluteAtmosphericPressure = inchOfMercuryToPascal(absbaromin)
    atmosphericPressure         = inchOfMercuryToPascal(baromin)
    rain                        = inchToMm(rainin)
    rainDaily                   = inchToMm(dailyrainin)
    rainWeekly                  = inchToMm(weeklyrainin)
    rainMonthly                 = inchToMm(monthlyrainin)
    temperature                 = fahrenheitToCelcius(tempf)
    dewPoint                    = fahrenheitToCelcius(dewptf)
    windChill                   = fahrenheitToCelcius(windchillf)
    windGust                    = mphToMps(windgustmph)
    windSpeed                   = mphToMps(windspeedmph)
    indoorTemperature           = fahrenheitToCelcius(indoortempf)
  } yield Measurement(
    timestamp,
    absoluteAtmosphericPressure,
    atmosphericPressure,
    rain,
    rainDaily,
    rainWeekly,
    rainMonthly,
    temperature,
    dewPoint,
    windChill,
    humidity,
    solarradiation,
    uv,
    winddir,
    windGust,
    windSpeed,
    indoorTemperature,
    indoorhumidity
  )

  private def timestamp(dateUtc: String): Either[String, Instant] =
    dateUtc match {
      case "now" => Left("Date 'now' is not supported")
      case _ =>
        Try(Instant.from(DateTimeFormat.parse(dateUtc)))
          .fold(_ => Left(s"Invalid date: $dateUtc"), Right(_))
    }

  private def inchOfMercuryToPascal(inches: Double): Double =
    3376.85 * inches // TODO depends on temp?

  private def inchToMm(inches: Double): Double =
    25.4 * inches

  private def fahrenheitToCelcius(fahrenheit: Double): Double =
    (fahrenheit - 32) / 1.8

  private def mphToMps(mph: Double): Double =
    1609.344 * mph / 3600
}

object QueryStringParameters {
  implicit val decoder: JsonDecoder[QueryStringParameters] =
    DeriveJsonDecoder.gen[QueryStringParameters]
}
