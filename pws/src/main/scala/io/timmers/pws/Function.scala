package io.timmers.pws

import java.io.{ InputStream, OutputStream }

import zio.Runtime
import zio.console.putStrLn
import zio.json.{ DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder }

import scala.io.Source

class Function {
  implicit val decoder: JsonDecoder[Measurement]          = DeriveJsonDecoder.gen[Measurement]
  implicit val requestDecoder: JsonDecoder[HttpRequest]   = DeriveJsonDecoder.gen[HttpRequest]
  implicit val responseEncoder: JsonEncoder[HttpResponse] = DeriveJsonEncoder.gen[HttpResponse]

  case class HttpRequest(
    headers: Map[String, String],
    queryStringParameters: Map[String, String],
    pathParameters: Map[String, String],
    body: String
  )

  case class HttpResponse(
    body: String,
    statusCode: Int = 200,
    headers: Map[String, String] = Map.empty,
    multiValueHeaders: Map[String, List[String]] = Map.empty,
    cookies: List[String] = List.empty,
    isBase64Encoded: Boolean = false
  )

  def logMeasurement(input: InputStream, output: OutputStream): Unit = {
    val jsonString = Source.fromInputStream(input).mkString
    val response   = putStrLn(s"REQUEST $jsonString").as(HttpResponse("ACK"))
    output.write(Runtime.default.unsafeRun(response).toJson.getBytes("UTF-8"))
  }
}
