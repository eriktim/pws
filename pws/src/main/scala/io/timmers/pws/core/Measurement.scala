package io.timmers.pws.core

import java.time.Instant

import zio.json.{ DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder }

case class Measurement(
  timestamp: Instant,
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
  windDirection: Int,
  windGust: Double,
  windSpeed: Double,
  indoorTemperature: Double,
  indoorHumidity: Int
)

object Measurement {
  implicit val encoder: JsonEncoder[Measurement] = DeriveJsonEncoder.gen[Measurement]

  implicit val decoder: JsonDecoder[Measurement] = DeriveJsonDecoder.gen[Measurement]
}
