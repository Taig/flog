package io.taig.flog.data

import cats.{Order, Show}
import cats.implicits._

sealed abstract class Level extends Product with Serializable

object Level {
  final case object Debug extends Level
  final case object Error extends Level
  final case object Info extends Level
  final case object Warning extends Level

  implicit val order: Order[Level] = Order.by {
    case Debug   => 0
    case Info    => 1
    case Warning => 2
    case Error   => 3
  }

  implicit val show: Show[Level] = {
    case Debug   => "debug"
    case Error   => "error"
    case Info    => "info"
    case Warning => "warning"
  }
}
