package io.timmers.pws.core

enum PwsError(message: String) extends Throwable:
  override def getMessage: String = message

  case MissingLogPath extends PwsError("Log path is not set as environment variable")

  case LogMeasurementError(message: String) extends PwsError(message)