package io.timmers.pws.core

import java.time.Instant
import scala.util.Try

case class Measurement(
  timestamp: Instant,
  absoluteBarometricPressure: Double,
  barometricPressure: Double,
  rain: Double,
  rainDaily: Double,
  rainWeekly: Double,
  rainMonthly: Double,
  temperature: Double,
  dewPoint: Double,
  windChill: Double,
  humidity: Int,
  solarRadiation: Double,
  uvIndex: Int,
  windDirection: Int,
  windGust: Double,
  windSpeed: Double,
  indoorTemperature: Double,
  indoorHumidity: Int
)

object Measurement:
  def fromLine(line: String): Option[Measurement] = line.split(",") match {
    case Array(
          timestamp,
          absoluteBarometricPressure,
          barometricPressure,
          rain,
          rainDaily,
          rainWeekly,
          rainMonthly,
          temperature,
          dewPoint,
          windChill,
          humidity,
          solarRadiation,
          uvIndex,
          windDirection,
          windGust,
          windSpeed,
          indoorTemperature,
          indoorHumidity
        ) =>
      Try(
        Measurement(
          Instant.parse(timestamp),
          absoluteBarometricPressure.toDouble,
          barometricPressure.toDouble,
          rain.toDouble,
          rainDaily.toDouble,
          rainWeekly.toDouble,
          rainMonthly.toDouble,
          temperature.toDouble,
          dewPoint.toDouble,
          windChill.toDouble,
          humidity.toInt,
          solarRadiation.toDouble,
          uvIndex.toInt,
          windDirection.toInt,
          windGust.toDouble,
          windSpeed.toDouble,
          indoorTemperature.toDouble,
          indoorHumidity.toInt
        )
      ).toOption
    case _ => None
  }

  implicit class MeasurementWrapper(private val measurement: Measurement) extends AnyVal {
    def header: String = measurement.productElementNames.mkString(",")

    def toLine: String = measurement.productIterator.map {
      case Some(value) => value
      case None        => ""
      case rest        => rest
    }
      .mkString(",")
  }
