package io.timmers.pws.core

import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Path, Paths, StandardOpenOption }
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.stream.Stream

import zio.blocking.Blocking
import zio.stream.{ Transducer, ZStream }
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

  def local(directory: String): ZLayer[Blocking, Nothing, Has[MeasurementLog]] =
    ZLayer.succeed {
      new MeasurementLog {
        override def append(measurement: Measurement): ZIO[Blocking, Throwable, Unit] = ZIO.effect {
          val timestamp = measurement.timestamp.atZone(ZoneOffset.UTC)
          val path = Paths.get(
            s"$directory/${timestamp.getYear}/${DateTimeFormatter.ISO_LOCAL_DATE.format(timestamp)}.log"
          )
          if (!Files.exists(path)) {
            if (!Files.exists(path.getParent)) {
              Files.createDirectories(path.getParent)
            }
            Files.writeString(path, s"${measurement.header}\n")
          }
          Files.writeString(
            path,
            s"${measurement.toLine}\n",
            StandardCharsets.UTF_8,
            StandardOpenOption.APPEND
          )
        }

        override def read(): ZStream[Blocking, Throwable, Measurement] = {
          val paths = ZIO.effect {
            val path = Paths.get(directory)
            Files
              .list(path)
              .flatMap(p => if (Files.isDirectory(p)) Files.list(p) else Stream.empty)
              .sorted()
          }
          ZStream
            .fromJavaStreamEffect(paths)
            .flatMap(readFile)
        }

        private def readFile(path: Path): ZStream[Blocking, Throwable, Measurement] =
          ZStream
            .fromFile(path)
            .aggregate(Transducer.utf8Decode)
            .aggregate(Transducer.splitLines)
            .drop(1)
            .flatMap(line =>
              Measurement
                .fromLine(line)
                .map(ZStream.succeed(_))
                .getOrElse(ZStream.fail(new RuntimeException(s"Failed parsing $line")))
            )
      }
    }
}
