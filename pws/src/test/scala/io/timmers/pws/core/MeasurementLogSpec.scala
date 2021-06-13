package io.timmers.pws.core

import java.time.Instant
import java.time.temporal.ChronoUnit

import zio.Chunk
import zio.test.Assertion.equalTo
import zio.test.{ DefaultRunnableSpec, ZSpec, assert }

object MeasurementLogSpec extends DefaultRunnableSpec {
  def spec: ZSpec[Environment, Failure] =
    suite("MeasurementLog Spec")(
      testM("should append to readable file") {
        val measurement1 = createMeasurement()
        val measurement2 = createMeasurement(timestamp = Instant.EPOCH.plus(1, ChronoUnit.MINUTES))
        val measurement3 = createMeasurement(timestamp = Instant.EPOCH.plus(1, ChronoUnit.DAYS))
        val measurement4 = createMeasurement(timestamp = Instant.EPOCH.plus(500, ChronoUnit.DAYS))
        val measurement5 = createMeasurement(timestamp = Instant.EPOCH.plus(1000, ChronoUnit.DAYS))
        for {
          _     <- MeasurementLog.append(measurement1)
          _     <- MeasurementLog.append(measurement2)
          _     <- MeasurementLog.append(measurement3)
          _     <- MeasurementLog.append(measurement4)
          lines <- MeasurementLog.read().runCollect.map(_.sortBy(_.timestamp))
          _     <- MeasurementLog.append(measurement5)
        } yield assert(lines)(
          equalTo(Chunk(measurement1, measurement2, measurement3, measurement4))
        )
      }
    ).provideCustomLayer(testEnvironment)

  private def createMeasurement(
    timestamp: Instant = Instant.EPOCH,
    absoluteAtmosphericPressure: Double = 0.0,
    atmosphericPressure: Double = 0.0,
    rain: Double = 0.0,
    rainDaily: Double = 0.0,
    rainWeekly: Double = 0.0,
    rainMonthly: Double = 0.0,
    temperature: Double = 0.0,
    dewPoint: Double = 0.0,
    windChill: Double = 0.0,
    humidity: Int = 0,
    solarRadiation: Double = 0.0,
    uvIndex: Int = 0,
    windDirection: Int = 0,
    windGust: Double = 0.0,
    windSpeed: Double = 0.0,
    indoorTemperature: Double = 0.0,
    indoorHumidity: Int = 0
  ) = Measurement(
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
    solarRadiation,
    uvIndex,
    windDirection,
    windGust,
    windSpeed,
    indoorTemperature,
    indoorHumidity
  )
}
