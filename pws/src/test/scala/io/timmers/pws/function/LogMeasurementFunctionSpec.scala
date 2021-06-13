package io.timmers.pws.function

import io.timmers.pws.aws.{ HttpRequest, HttpResponse, runLambda }

import zio.nio.file.Files
import zio.test.Assertion.equalTo
import zio.test.{ DefaultRunnableSpec, ZSpec, assert }

object LogMeasurementFunctionSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] =
    suite("LogMeasurementFunction Spec")(
      test("should not accept empty requests") {
        val request  = HttpRequest()
        val response = logMeasurement(request)
        assert(response.statusCode)(equalTo(400))
      },
      test("should not accept unknown actions") {
        val request  = httpRequest(action = "dummy")
        val response = logMeasurement(request)
        assert(response.statusCode)(equalTo(400))
      },
      test("should not accept unauthorized requests") {
        val request  = httpRequest()
        val response = logMeasurement(request, id = "wrong-id", password = "wrong-password")
        assert(response.statusCode)(equalTo(400))
      },
      test("should not accept invalid dates") {
        val request  = httpRequest(dateutc = "now")
        val response = logMeasurement(request)
        assert(response.statusCode)(equalTo(400))
      },
      testM("should handle requests") {
        val request = httpRequest()
        for {
          path    <- Files.createTempDirectory(Some("pws-"), Seq())
          response = logMeasurement(request, path = path.toString())
        } yield assert(response.statusCode)(equalTo(204))
      }
    )

  private def logMeasurement(
    request: HttpRequest,
    id: String = "test-id",
    password: String = "test-password",
    path: String = null
  ): HttpResponse = {
    val envs = Map("PWS_ID" -> id, "PWS_PASSWORD" -> password, "PWS_PATH" -> path)
    runLambda(new LogMeasurementFunction(), request, envs)
  }

  private def httpRequest(
    dateutc: String = "2020-01-01 00:00:00",
    action: String = "updateraw"
  ) = {
    val queryStringParameters = Map(
      "ID"             -> "test-id",
      "PASSWORD"       -> "test-password",
      "dateutc"        -> dateutc,
      "absbaromin"     -> "29.891",
      "action"         -> action,
      "baromin"        -> "29.879",
      "dailyrainin"    -> "0.000",
      "dewptf"         -> "30.9",
      "humidity"       -> "45",
      "indoorhumidity" -> "35",
      "indoortempf"    -> "69.8",
      "monthlyrainin"  -> "1.681",
      "rainin"         -> "0.000",
      "realtime"       -> "1",
      "rtfreq"         -> "5",
      "softwaretype"   -> "EasyWeatherV1.4.6",
      "solarradiation" -> "358.69",
      "tempf"          -> "51.6",
      "UV"             -> "5",
      "weeklyrainin"   -> "0.000",
      "windchillf"     -> "51.6",
      "winddir"        -> "166",
      "windgustmph"    -> "5.8",
      "windspeedmph"   -> "3.1"
    )
    HttpRequest(queryStringParameters = queryStringParameters)
  }
}
