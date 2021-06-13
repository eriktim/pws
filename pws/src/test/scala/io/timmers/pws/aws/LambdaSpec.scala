package io.timmers.pws.aws

import java.io.{ ByteArrayOutputStream, InputStream }
import java.nio.charset.StandardCharsets

import zio.json.DecoderOps
import zio.test.Assertion.equalTo
import zio.test.{ DefaultRunnableSpec, ZSpec, assert }
import zio.{ ZEnv, ZIO }

object LambdaSpec extends DefaultRunnableSpec {
  def spec: ZSpec[Environment, Failure] =
    suite("Lambda Spec")(
      test("should not accept empty requests") {
        val response = handle(InputStream.nullInputStream())
        assert(response.statusCode)(equalTo(400))
      },
      test("should not accept malformed requests") {
        val response = handle(getClass.getResourceAsStream("/bad-request.json"))
        assert(response.statusCode)(equalTo(400))
      },
      test("should accept valid requests") {
        val response = handle(getClass.getResourceAsStream("/request.json"))
        assert(response.statusCode)(equalTo(200))
      }
    )

  private def handle(inputStream: InputStream) = {
    val outputStream = new ByteArrayOutputStream()
    new EchoLambda().handle(inputStream, outputStream)
    new String(outputStream.toByteArray, StandardCharsets.UTF_8)
      .fromJson[HttpResponse]
      .getOrElse(HttpResponse.badRequest)
  }
}

class EchoLambda extends Lambda {
  override def handleRequest(
    request: HttpRequest
  ): ZIO[ZEnv, Throwable, HttpResponse] = ZIO.succeed(HttpResponse(body = request.body))
}
