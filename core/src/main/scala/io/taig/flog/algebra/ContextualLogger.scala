package io.taig.flog.algebra

import java.util.UUID

import cats.FlatMap
import cats.effect.Sync
import cats.implicits._
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.HasFiberRef
import io.taig.flog.data.{Context, Event}
import io.taig.flog.util.Circe

abstract class ContextualLogger[F[_]: FlatMap](logger: Logger[F])
    extends Logger[F] {
  override final def log(event: Long => Event): F[Unit] =
    context.flatMap { context =>
      logger.log { timestamp =>
        val raw = event(timestamp)
        raw.copy(
          scope = context.prefix ++ raw.scope,
          payload = Circe.combine(context.payload, raw.payload)
        )
      }
    }

  def context: F[Context]

  def locally[A](f: Context => Context)(run: F[A]): F[A]

  final def traced[A](run: F[A])(implicit F: Sync[F]): F[A] =
    for {
      uuid <- F.delay(UUID.randomUUID())
      result <- locally(_.combine(JsonObject("trace" := uuid)))(run)
    } yield result
}

object ContextualLogger {
  def apply[F[_]: FlatMap: HasFiberRef](
      logger: Logger[F]
  ): F[ContextualLogger[F]] =
    HasFiberRef[F].make(Context.Empty).map { ref =>
      new ContextualLogger[F](logger) {
        override val context: F[Context] = ref.get

        override def locally[A](f: Context => Context)(run: F[A]): F[A] =
          context.flatMap(context => ref.locally(f(context))(run))
      }
    }
}
