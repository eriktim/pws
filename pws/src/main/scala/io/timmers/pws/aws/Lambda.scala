package io.timmers.pws.aws

import java.io.{ InputStream, OutputStream }
import java.nio.charset.StandardCharsets

import scala.io.Source

import zio.console.putStrLnErr
import zio.json.{ DecoderOps, EncoderOps }
import zio.{ Runtime, ZEnv, ZIO }

abstract class Lambda {
  def handle(input: InputStream, output: OutputStream): Unit =
    runHandle(input, output, Runtime.default)

  private[aws] def runHandle(
    input: InputStream,
    output: OutputStream,
    runtime: Runtime[ZEnv]
  ): Unit = {
    val jsonString = Source.fromInputStream(input).mkString
    val request    = jsonString.fromJson[HttpRequest]
    val response: ZIO[ZEnv, Nothing, HttpResponse] = ZIO
      .fromEither(request)
      .flatMap(handleRequest(_).mapError(_.getMessage))
      .tapError(err => putStrLnErr(s"Failed handling request: $err"))
      .orElseSucceed(HttpResponse.badRequest)
    output.write(
      runtime
        .unsafeRun(response)
        .toJson
        .getBytes(StandardCharsets.UTF_8)
    )
  }

  protected def handleRequest(
    request: HttpRequest
  ): ZIO[ZEnv, Throwable, HttpResponse]

}
