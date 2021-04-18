package io.timmers.pws

import zio._
import zio.console.putStrLn

object Application extends App {
  val TestFile = "/home/erik/git/pws/test.log"

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    app()
      .provideCustomLayer(MeasurementLog.localFile(TestFile))
      .exitCode

  private def app() = for {
    _ <- MeasurementLog.append(Measurement("lots of data"))
    _ <- MeasurementLog.read().foreach(m => putStrLn(m.line))
  } yield ()

}
