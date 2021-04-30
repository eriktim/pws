package io.timmers.pws.core

import java.nio.file.Files
import java.time.Instant

import zio.Chunk
import zio.test.Assertion.equalTo
import zio.test.environment.liveEnvironment
import zio.test.{ DefaultRunnableSpec, ZSpec, assert }

object MeasurementLogSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] =
    suite("MeasurementLog Spec")(
      testM("should append to readable file") {
        val measurement1 = measurement()
        val measurement2 = measurement(rain = 1.0)
        val measurement3 = measurement(rain = 2.0)
        for {
          _     <- MeasurementLog.append(measurement1)
          _     <- MeasurementLog.append(measurement2)
          lines <- MeasurementLog.read().runCollect
          _     <- MeasurementLog.append(measurement3)
        } yield assert(lines)(equalTo(Chunk(measurement1, measurement2)))
      }
    ).provideCustomLayer(
      liveEnvironment >>> MeasurementLog.local(Files.createTempDirectory("pws-").toString)
    )

  private def measurement(
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
