package io.timmers.pws.aws

import zio.json.{ DeriveJsonDecoder, JsonDecoder }

case class HttpRequest(
  headers: Map[String, String] = Map.empty,
  queryStringParameters: Map[String, String] = Map.empty,
  pathParameters: Map[String, String] = Map.empty,
  body: Option[String] = None
)

object HttpRequest {
  implicit val decoder: JsonDecoder[HttpRequest] = DeriveJsonDecoder.gen[HttpRequest]
}
