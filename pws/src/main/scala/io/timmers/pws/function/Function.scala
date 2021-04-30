package io.timmers.pws.function

import java.io.{ InputStream, OutputStream }
import java.nio.charset.StandardCharsets

import scala.io.Source

import io.timmers.pws.core.MeasurementLog

import zio.Runtime
import zio.console.putStrLn
import zio.json.{ DecoderOps, EncoderOps }

class Function {
  def logMeasurement(input: InputStream, output: OutputStream): Unit = {
    val jsonString = Source.fromInputStream(input).mkString
    val response = readParams(jsonString)
      .flatMap(_.toMeasurement)
      .fold(
        error => putStrLn(s"FAILED $error").as(HttpResponse(error, statusCode = 400)),
        measurement =>
          for {
            _ <- putStrLn(s"MEASUREMENT $measurement")
            _ <- MeasurementLog.append(measurement)
          } yield HttpResponse("ACK")
      )
    output.write(
      Runtime.default
        .unsafeRun(response.provideCustomLayer(MeasurementLog.local("/tmp/pws")))
        .toJson
        .getBytes(StandardCharsets.UTF_8)
    )
  }

  private def readParams(input: String): Either[String, QueryStringParameters] =
    input
      .fromJson[HttpRequest]
      .map(request => request.queryStringParameters)
      .flatMap { params =>
        if (params.action != "updateraw") {
          return Left(s"Invalid action: $params.action")
        }
        if (params.id != "timmers" || params.password != "secret") { // FIXME
          return Left(s"Unauthorized")
        }
        return Right(params);
      }
}
