package io.timmers.pws.function

import zio.json.{ DeriveJsonDecoder, JsonDecoder }

case class HttpRequest(
  headers: Map[String, String] = Map.empty,
  queryStringParameters: QueryStringParameters,
  pathParameters: Map[String, String] = Map.empty,
  body: String = ""
)
object HttpRequest {
  implicit val decoder: JsonDecoder[HttpRequest] = DeriveJsonDecoder.gen[HttpRequest]
}
