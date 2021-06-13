package io.timmers.pws.aws

import zio.json.{ DeriveJsonEncoder, JsonEncoder }

case class HttpResponse(
  body: Option[String] = None,
  statusCode: Int = 200,
  headers: Map[String, String] = Map.empty,
  multiValueHeaders: Map[String, List[String]] = Map.empty,
  cookies: List[String] = List.empty,
  isBase64Encoded: Boolean = false
)

object HttpResponse {
  implicit val encoder: JsonEncoder[HttpResponse] = DeriveJsonEncoder.gen[HttpResponse]

  def ok(body: String): HttpResponse = HttpResponse(body = Some(body))

  val noContent: HttpResponse = HttpResponse(statusCode = 204)

  val badRequest: HttpResponse = HttpResponse(statusCode = 400)
}
