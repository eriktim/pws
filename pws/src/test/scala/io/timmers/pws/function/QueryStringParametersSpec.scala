package io.timmers.pws.function

import java.time.Instant

import io.timmers.pws.core.Measurement

import zio.test.Assertion.equalTo
import zio.test.{ DefaultRunnableSpec, ZSpec, assert }

object QueryStringParametersSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] =
    suite("QueryStringParameters Spec")(
      test("should convert to Measurement") {
        val params = QueryStringParameters(
          "ID",
          "secret",
          "1970-01-01 00:00:00",
          0,
          30.000,
          "updateraw",
          29.988,
          0.000,
          30.4,
          41,
          32,
          72.5,
          1.681,
          0.000,
          1,
          5,
          "EasyWeatherV1.4.6",
          11.62,
          53.4,
          0.000,
          53.4,
          77,
          2.2,
          1.6
        )
        val measurement = Measurement(
          Instant.EPOCH,
          101305.5,
          101264.9778,
          0.0,
          0.0,
          0.0,
          42.6974,
          11.888888888888888,
          -0.8888888888888896,
          11.888888888888888,
          41,
          11.62,
          0,
          77,
          0.983488,
          0.715264,
          22.5,
          32
        )
        assert(params.toMeasurement)(equalTo(Right(measurement)))
      }
    )
}
