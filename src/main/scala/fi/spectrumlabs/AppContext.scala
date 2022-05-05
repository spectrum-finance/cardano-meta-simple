package fi.spectrumlabs

import derevo.derive
import io.estatico.newtype.ops._
import tofu.WithContext
import tofu.logging.derivation.{hidden, loggable}
import tofu.optics.macros.{promote, ClassyOptics}

@ClassyOptics
@derive(loggable)
final case class AppContext(
  @promote traceId: String
)

object AppContext extends WithContext.Companion[AppContext] {

  def init: AppContext = AppContext("<Root>")
}