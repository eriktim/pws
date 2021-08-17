package io.timmers.pws.wunderground

import io.timmers.pws.core.Measurement

import java.time.format.DateTimeFormatterBuilder
import java.time.{Instant, ZoneOffset}
import scala.util.Try

// https://support.weather.com/s/article/PWS-Upload-Protocol?language=en_US
case class WundergroundAction(
  // ID as registered by wunderground.com (required)
  id: String,
  // Station Key registered with this PWS ID, case sensitive (required)
  password: String,
  // YYYY-MM-DD HH:MM:SS in UTC or "now" (required)
  dateutc: String,
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
  uv: Int,
  // rain inches over the past week
  weeklyrainin: Double,
  windchillf: Double,
  // 0-360 instantaneous wind direction
  winddir: Int,
  // mph current wind gust, using software specific time period
  windgustmph: Double,
  // mph instantaneous wind speed
  windspeedmph: Double
):
  private val DateTimeFormat = new DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd HH:mm:ss")
    .toFormatter()
    .withZone(ZoneOffset.UTC);

  def toMeasurement: Either[String, Measurement] =
    for {
      timestamp                  <- parseDateUtc(dateutc)
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

  private def parseDateUtc(dateUtc: String): Either[String, Instant] =
    Try(Instant.from(DateTimeFormat.parse(dateUtc))).fold(e => Left(e.getMessage), Right(_))

  private def inchOfMercuryToPascal(inches: Double): Double =
    3386.389 * inches

  private def inchToMm(inches: Double): Double =
    25.4 * inches

  private def fahrenheitToCelcius(fahrenheit: Double): Double =
    (fahrenheit - 32) / 1.8

  private def mphToMps(mph: Double): Double =
    1609.344 * mph / 3600

object WundergroundAction:
  // FIXME constants

  def fromMap(data: Map[String, String]): Option[WundergroundAction] = for {
    id             <- data.get("ID")
    password       <- data.get("PASSWORD")
    dateutc        <- data.get("dateutc")
    absbaromin     <- data.get("absbaromin").map(_.toDouble)
    action         <- data.get("action")
    baromin        <- data.get("baromin").map(_.toDouble)
    dailyrainin    <- data.get("dailyrainin").map(_.toDouble)
    dewptf         <- data.get("dewptf").map(_.toDouble)
    humidity       <- data.get("humidity").map(_.toInt)
    indoorhumidity <- data.get("indoorhumidity").map(_.toInt)
    indoortempf    <- data.get("indoortempf").map(_.toDouble)
    monthlyrainin  <- data.get("monthlyrainin").map(_.toDouble)
    rainin         <- data.get("rainin").map(_.toDouble)
    realtime       <- data.get("realtime").map(_.toInt)
    rtfreq         <- data.get("rtfreq").map(_.toInt)
    softwaretype   <- data.get("softwaretype")
    solarradiation <- data.get("solarradiation").map(_.toDouble)
    tempf          <- data.get("tempf").map(_.toDouble)
    uv             <- data.get("UV").map(_.toInt)
    weeklyrainin   <- data.get("weeklyrainin").map(_.toDouble)
    windchillf     <- data.get("windchillf").map(_.toDouble)
    winddir        <- data.get("winddir").map(_.toInt)
    windgustmph    <- data.get("windgustmph").map(_.toDouble)
    windspeedmph   <- data.get("windspeedmph").map(_.toDouble)
  } yield WundergroundAction(
    id = id,
    password = password,
    dateutc = dateutc,
    uv = uv,
    absbaromin = absbaromin,
    action = action,
    baromin = baromin,
    dailyrainin = dailyrainin,
    dewptf = dewptf,
    humidity = humidity,
    indoorhumidity = indoorhumidity,
    indoortempf = indoortempf,
    monthlyrainin = monthlyrainin,
    rainin = rainin,
    realtime = realtime,
    rtfreq = rtfreq,
    softwaretype = softwaretype,
    solarradiation = solarradiation,
    tempf = tempf,
    weeklyrainin = weeklyrainin,
    windchillf = windchillf,
    winddir = winddir,
    windgustmph = windgustmph,
    windspeedmph = windspeedmph
  )
