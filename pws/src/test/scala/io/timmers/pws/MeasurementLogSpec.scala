package io.timmers.pws

import zio.Chunk
import zio.test.Assertion.equalTo
import zio.test.environment.liveEnvironment
import zio.test.{ DefaultRunnableSpec, ZSpec, assert }

object MeasurementLogSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] =
    suite("MeasurementLog Spec")(
      testM("should append to readable file") {
        for {
          _     <- MeasurementLog.append(Measurement("foo"))
          _     <- MeasurementLog.append(Measurement("bar"))
          lines <- MeasurementLog.read().runCollect
          _     <- MeasurementLog.append(Measurement("baz"))
        } yield assert(lines)(equalTo(Chunk(Measurement("foo"), Measurement("bar"))))
      }
    ).provideCustomLayer(liveEnvironment >>> MeasurementLog.localFile("/tmp/pws-test.log"))
}
