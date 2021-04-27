package io.timmers.pws.core

import java.nio.charset.StandardCharsets
import java.nio.file.{ Paths, StandardOpenOption }

import zio.blocking.Blocking
import zio.json.{ DecoderOps, EncoderOps }
import zio.stream.{ Transducer, ZSink, ZStream }
import zio.{ Has, ZIO, ZLayer }

trait MeasurementLog {
  def append(measurement: Measurement): ZIO[Blocking, Throwable, Unit]

  def read(): ZStream[Blocking, Throwable, Measurement]
}

object MeasurementLog {
  def append(measurement: Measurement): ZIO[Has[MeasurementLog] with Blocking, Throwable, Unit] =
    ZIO.accessM(_.get.append(measurement))

  def read(): ZStream[Has[MeasurementLog] with Blocking, Throwable, Measurement] =
    ZStream.accessStream(_.get.read())

  def localFile(filename: String): ZLayer[Blocking, Nothing, Has[MeasurementLog]] = ZLayer.succeed {
    val path = Paths.get(filename)
    new MeasurementLog {
      override def append(measurement: Measurement): ZIO[Blocking, Throwable, Unit] = {
        val sink =
          ZSink.fromFile(
            path,
            options =
              Set(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)
          )
        ZStream
          .fromIterable(s"${measurement.toJson}\n".getBytes(StandardCharsets.UTF_8))
          .run(sink)
          .unit
      }

      override def read(): ZStream[Blocking, Throwable, Measurement] =
        ZStream
          .fromFile(path)
          .aggregate(Transducer.utf8Decode)
          .aggregate(Transducer.splitLines)
          .flatMap(line =>
            line.fromJson[Measurement] match {
              case Left(error)        => ZStream.fail(new RuntimeException(error))
              case Right(measurement) => ZStream.succeed(measurement)
            }
          )
    }
  }
}
