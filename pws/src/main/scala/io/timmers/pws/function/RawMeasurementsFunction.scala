package io.timmers.pws.function

import io.timmers.pws.aws.{HttpRequest, HttpResponse, Lambda}
import io.timmers.pws.core.MeasurementLog
import zio.json.EncoderOps
import zio.{ZEnv, ZIO}

class RawMeasurementsFunction extends Lambda:
  override protected def handleRequest(
    request: HttpRequest
  ): ZIO[ZEnv, Throwable, HttpResponse] = for {
    lines <- MeasurementLog
               .read()
               .runCollect
               .map(_.map(_.toLine))
               .provideCustomLayer(MeasurementLog.live)
  } yield HttpResponse.ok(lines.toJson)
