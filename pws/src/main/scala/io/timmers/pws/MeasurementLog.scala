package io.timmers.pws

import zio.blocking.Blocking
import zio.stream.{ Transducer, ZSink, ZStream }
import zio.{ Has, ZIO, ZLayer }

import java.nio.charset.StandardCharsets
import java.nio.file.{ Path, Paths, StandardOpenOption }

trait MeasurementLog {
  def append(measurement: Measurement): ZIO[Blocking, Throwable, Unit]

  def read(): ZStream[Blocking, Throwable, Measurement]
}

object MeasurementLog {
  def append(measurement: Measurement): ZIO[Has[MeasurementLog] with Blocking, Throwable, Unit] =
    ZIO.accessM(_.get.append(measurement))

  def read(): ZStream[Has[MeasurementLog] with Blocking, Throwable, Measurement] =
    ZStream.accessStream(_.get.read())

  case class LocalMeasurementLog(path: Path) extends MeasurementLog {
    override def append(measurement: Measurement): ZIO[Blocking, Throwable, Unit] = {
      val sink =
        ZSink.fromFile(
          path,
          options =
            Set(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)
        )
      ZStream
        .fromIterable(s"${measurement.line}\n".getBytes(StandardCharsets.UTF_8))
        .run(sink)
        .unit
    }

    override def read(): ZStream[Blocking, Throwable, Measurement] =
      ZStream
        .fromFile(path)
        .aggregate(Transducer.utf8Decode)
        .aggregate(Transducer.splitLines)
        .map(line => Measurement(line))
  }

  def localFile(filename: String): ZLayer[Blocking, Nothing, Has[MeasurementLog]] =
    ZLayer.succeed(LocalMeasurementLog(Paths.get(filename)))
}
