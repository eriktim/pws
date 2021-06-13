package io.timmers.pws

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, IOException }
import java.nio.charset.StandardCharsets

import zio.Runtime
import zio.json.{
  DecoderOps,
  DeriveJsonDecoder,
  DeriveJsonEncoder,
  EncoderOps,
  JsonDecoder,
  JsonEncoder
}
import zio.test.environment.{ TestSystem, liveEnvironment }

package object aws {
  def runLambda(
    lambda: Lambda,
    request: HttpRequest,
    envs: Map[String, String] = Map.empty
  ): HttpResponse = {
    val testEnvironment = liveEnvironment ++ TestSystem.live(TestSystem.Data(envs = envs))
    val testRuntime     = Runtime.unsafeFromLayer(testEnvironment)
    val inputStream     = new ByteArrayInputStream(request.toJson.getBytes(StandardCharsets.UTF_8))
    val outputStream    = new ByteArrayOutputStream()
    lambda.runHandle(inputStream, outputStream, testRuntime)
    outputStream.toString(StandardCharsets.UTF_8).fromJson[HttpResponse] match {
      case Right(response) => response
      case Left(err)       => throw new IOException(err)
    }
  }

  implicit val requestEncoder: JsonEncoder[HttpRequest]   = DeriveJsonEncoder.gen[HttpRequest]
  implicit val responseDecoder: JsonDecoder[HttpResponse] = DeriveJsonDecoder.gen[HttpResponse]
}
