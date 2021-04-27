package io.timmers.pws.function

import zio.json.{ DeriveJsonEncoder, JsonEncoder }

case class HttpResponse(
  body: String,
  statusCode: Int = 200,
  headers: Map[String, String] = Map.empty,
  multiValueHeaders: Map[String, List[String]] = Map.empty,
  cookies: List[String] = List.empty,
  isBase64Encoded: Boolean = false
)

object HttpResponse {
  implicit val encoder: JsonEncoder[HttpResponse] = DeriveJsonEncoder.gen[HttpResponse]
}
