package io.taig.flog.data

import io.taig.flog.Encoder
import io.taig.flog.syntax._
import io.taig.flog.util.StacktracePrinter

final case class Event(
    timestamp: Long,
    level: Level,
    scope: Scope,
    message: String,
    payload: Payload.Object,
    throwable: Option[Throwable]
) {
  def modifyTimestamp(f: Long => Long): Event = copy(timestamp = f(timestamp))

  def withTimestamp(timestamp: Long): Event = modifyTimestamp(_ => timestamp)

  def modifyLevel(f: Level => Level): Event = copy(level = f(level))

  def withLevel(level: Level): Event = modifyLevel(_ => level)

  def modifyScope(f: Scope => Scope): Event = copy(scope = f(scope))

  def withScope(scope: Scope): Event = modifyScope(_ => scope)

  def append(scope: Scope): Event = modifyScope(_ ++ scope)

  def prepend(scope: Scope): Event = modifyScope(scope ++ _)

  def modifyMessage(f: String => String): Event = copy(message = f(message))

  def withMessage(message: String): Event = modifyMessage(_ => message)

  def modifyPayload(f: Payload.Object => Payload.Object): Event = copy(payload = f(payload))

  def withPayload(payload: Payload.Object): Event = modifyPayload(_ => payload)

  def merge(payload: Payload.Object): Event = modifyPayload(_ deepMerge payload)

  def withContext(context: Context): Event = prepend(context.prefix).modifyPayload(context.presets deepMerge _)
}

object Event {
  implicit val encoder: Encoder.Object[Event] = event =>
    Payload.of(
      "timestamp" := event.timestamp,
      "level" := event.level,
      "scope" := event.scope,
      "message" := Some(event.message).filter(_.nonEmpty),
      "payload" := event.payload,
      "stacktrace" := event.throwable.map(StacktracePrinter(_))
    )
}
