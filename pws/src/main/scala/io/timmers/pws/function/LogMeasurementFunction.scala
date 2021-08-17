package io.timmers.pws.function

import io.timmers.pws.aws.{ HttpRequest, HttpResponse, Lambda }
import io.timmers.pws.core.PwsError.LogMeasurementError
import io.timmers.pws.core.{ Measurement, MeasurementLog }
import io.timmers.pws.wunderground.WundergroundAction

import zio.console.putStrLn
import zio.{ ZEnv, ZIO, system }

class LogMeasurementFunction extends Lambda {
  override protected def handleRequest(
    request: HttpRequest
  ): ZIO[ZEnv, Throwable, HttpResponse] = for {
    id       <- system.envOrElse("PWS_ID", "")
    password <- system.envOrElse("PWS_PASSWORD", "")
    measurement <- ZIO
                     .fromEither(toMeasurement(id, password)(request))
                     .mapError(LogMeasurementError(_))
    _ <- putStrLn(s"Received measurement: $measurement")
    _ <- MeasurementLog
           .append(measurement)
           .provideCustomLayer(MeasurementLog.live)
  } yield HttpResponse.noContent

  private def toMeasurement(id: String, password: String)(
    request: HttpRequest
  ): Either[String, Measurement] =
    WundergroundAction.fromMap(request.queryStringParameters) match {
      case None => Left("Bad request parameters")
      case Some(action) =>
        if (action.action != "updateraw") {
          Left(s"Unsupported action '${action.action}'")
        } else if (action.id != id || action.password != password) {
          Left("Unauthorized")
        } else {
          action.toMeasurement
        }
    }
}
