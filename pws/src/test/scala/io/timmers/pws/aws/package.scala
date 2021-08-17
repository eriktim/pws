package io.timmers.pws

import zio.Runtime
import zio.json.*
import zio.test.environment.{TestSystem, liveEnvironment}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException}
import java.nio.charset.StandardCharsets

package object aws:
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
