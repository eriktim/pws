package io.timmers.pws.core

import io.timmers.pws.core.PwsError.MissingLogPath
import zio.blocking.Blocking
import zio.nio.core.file.Path
import zio.nio.file.Files
import zio.stream.ZStream
import zio.{Has, ZIO, ZLayer, system}

import java.io.IOException
import java.nio.file.StandardOpenOption
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// TODO Non-blocking, IOException
trait MeasurementLog:
  def append(measurement: Measurement): ZIO[Blocking, Throwable, Unit]

  def read(): ZStream[Blocking, Throwable, Measurement]

object MeasurementLog:
  def append(measurement: Measurement): ZIO[Has[MeasurementLog] with Blocking, Throwable, Unit] =
    ZIO.accessM(_.get.append(measurement))

  def read(): ZStream[Has[MeasurementLog] with Blocking, Throwable, Measurement] =
    ZStream.accessStream(_.get.read())

  def live: ZLayer[system.System, Throwable, Has[MeasurementLog]] =
    system
      .env("PWS_PATH")
      .flatMap(ZIO.fromOption(_))
      .mapBoth(
        _ => MissingLogPath,
        directory =>
          new MeasurementLog {
            override def append(measurement: Measurement): ZIO[Blocking, Throwable, Unit] = {
              val timestamp = measurement.timestamp.atZone(ZoneOffset.UTC)
              val path = Path(
                directory,
                timestamp.getYear.toString,
                s"${DateTimeFormatter.ISO_LOCAL_DATE.format(timestamp)}.log"
              )
              for {
                directory <- ZIO.fromOption(path.parent).orElseFail(new IOException())
                _         <- Files.createDirectories(directory).unlessM(Files.exists(directory))
                _ <-
                  Files.writeLines(path, Seq(s"${measurement.header}")).unlessM(Files.exists(path))
                _ <- Files.writeLines(
                       path,
                       Seq(measurement.toLine),
                       openOptions = Set(StandardOpenOption.APPEND)
                     )
              } yield ()
            }

            override def read(): ZStream[Blocking, Throwable, Measurement] = {
              val path = Path(directory)
              Files
                .list(path)
                .flatMap(Files.list)
                .flatMap(readFile)
            }

            private def readFile(path: Path): ZStream[Blocking, Throwable, Measurement] =
              ZStream
                .fromIteratorEffect(Files.readAllLines(path).map(_.drop(1).reverseIterator))
                .flatMap(line =>
                  Measurement
                    .fromLine(line)
                    .map(ZStream.succeed(_))
                    .getOrElse(ZStream.fail(new IOException(s"Failed parsing $line")))
                )
          }
      )
      .toLayer