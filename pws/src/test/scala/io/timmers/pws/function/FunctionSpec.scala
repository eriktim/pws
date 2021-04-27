package io.timmers.pws.function

import java.io.ByteArrayOutputStream

import zio.ZIO
import zio.test.Assertion.isGreaterThan
import zio.test.{ DefaultRunnableSpec, ZSpec, assert }

object FunctionSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] =
    suite("Function Spec")(
      testM("should handle requests") {
        for {
          inputStream <- ZIO.effect(getClass.getResourceAsStream("/request.json"))
          outputStream = new ByteArrayOutputStream()
          _            = new Function().logMeasurement(inputStream, outputStream)
        } yield assert(outputStream.size())(isGreaterThan(0))
      }
    )
}
