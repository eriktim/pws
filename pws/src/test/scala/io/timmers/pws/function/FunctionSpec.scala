package io.timmers.pws.function

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

import zio.ZIO
import zio.json.{ DecoderOps, DeriveJsonDecoder, JsonDecoder }
import zio.test.Assertion.equalTo
import zio.test.{ DefaultRunnableSpec, ZSpec, assert }

object FunctionSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] =
    suite("Function Spec")(
      testM("should handle requests") {
        val outputStream = new ByteArrayOutputStream()
        for {
          inputStream <- ZIO.effect(getClass.getResourceAsStream("/request.json"))
          _            = new Function().logMeasurement(inputStream, outputStream)
          response <- ZIO.fromEither(
                        new String(outputStream.toByteArray, StandardCharsets.UTF_8)
                          .fromJson[HttpResponse]
                      )
        } yield assert(response.statusCode)(equalTo(200)) &&
          assert(response.body)(equalTo("ACK"))
      }
    )

  implicit val decoder: JsonDecoder[HttpResponse] =
    DeriveJsonDecoder.gen[HttpResponse]
}
