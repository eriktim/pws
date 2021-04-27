package io.timmers.pws.function

import zio.json.{ DeriveJsonDecoder, JsonDecoder }

case class HttpRequest(
  headers: Map[String, String],
  queryStringParameters: QueryStringParameters,
  pathParameters: Map[String, String],
  body: String
)
object HttpRequest {
  implicit val decoder: JsonDecoder[HttpRequest] = DeriveJsonDecoder.gen[HttpRequest]
}
