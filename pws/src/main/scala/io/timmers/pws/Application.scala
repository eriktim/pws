package io.timmers.pws

import zio._
import zio.console.putStrLn
import zio.stream.{ Transducer, ZSink, ZStream }

import java.nio.file.{ Files, Paths, StandardOpenOption }
import java.time.Duration

object Application extends App {
  val TestFile = "/home/erik/git/pws/test.log"

  private val appendResource = Managed.make(
    ZIO.effect(Files.newOutputStream(Paths.get(TestFile), StandardOpenOption.APPEND))
  )(outputStream => UIO(outputStream.close()))

  private val readResource = Managed.make(
    ZIO.effect(Files.newInputStream(Paths.get(TestFile)))
  )(inputStream => UIO(inputStream.close()))

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    app().exitCode

  private def app() = for {
    fiber <- append("hello world")
               .repeat(Schedule.exponential(Duration.ofMillis(5), 2.0))
               .fork
    _ <- cat()
           .repeat(Schedule.exponential(Duration.ofMillis(5), 1.5))
    _ <- fiber.join
  } yield ()

  private def append(text: String) = for {
    _ <- putStrLn("APPEND")
    _ <- appendResource.use { outputStream =>
           ZStream
             .fromIterable(s"$text\n".getBytes("UTF-8"))
             .run(ZSink.fromOutputStream(outputStream))
         }
  } yield ()

  private def cat() = for {
    _ <- putStrLn("CAT")
    _ <- readResource.use { inputStream =>
           ZStream
             .fromInputStream(inputStream)
             .aggregate(Transducer.utf8Decode)
             .aggregate(Transducer.splitLines)
             .tap { data =>
               putStrLn(s"> $data")
             }
             .runDrain
         }
  } yield ()
}
