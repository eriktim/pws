package io.timmers.pws.core

sealed trait PwsError extends Throwable {
  def message: String

  override def getMessage: String = message
}

object PwsError {
  final case object MissingLogPath extends PwsError {
    def message = "Log path is not set as environment variable"
  }

  final case class LogMeasurementError(message: String) extends PwsError {}
}
