package io.timmers.pws

import io.timmers.pws.MeasurementLog.LocalMeasurementLog
import zio.test.Assertion.equalTo
import zio.test.environment.liveEnvironment
import zio.test.{ DefaultRunnableSpec, ZSpec, assert }
import zio.{ Chunk, Managed, Task, ZIO }

import java.nio.file.Files

object MeasurementLogSpec extends DefaultRunnableSpec {
  private val layer = Managed
    .make(Task(Files.createTempFile("stream", "fromFile")))(path => Task(Files.delete(path)).orDie)
    .use(path => ZIO.succeed(LocalMeasurementLog(path)))
    .toLayer

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
    ).provideCustomLayer(liveEnvironment >>> layer)
}
