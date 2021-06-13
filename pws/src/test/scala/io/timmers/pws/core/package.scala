package io.timmers.pws

import zio.nio.file.Files
import zio.test.environment.{ TestSystem, liveEnvironment }
import zio.{ Has, Runtime, ZLayer }

package object core {
  val testEnvironment: ZLayer[Any with Any, Nothing, Has[MeasurementLog]] = {
    val pathEffect = Files.createTempDirectory(Some("pws-"), Seq())
    val path       = Runtime.default.unsafeRun(pathEffect)
    liveEnvironment ++
      TestSystem.live(TestSystem.Data(envs = Map("PWS_PATH" -> path.toString()))) >>>
      MeasurementLog.live.orDie
  }
}
