package fi.spectrumlabs

import derevo.derive
import derevo.pureconfig.pureconfigReader
import tofu.logging.derivation.loggable

import scala.concurrent.duration.FiniteDuration

@derive(loggable, pureconfigReader)
final case class AppConfig(pg: PgConfig, http: HttpAppConfig)

@derive(loggable, pureconfigReader)
final case class PgConfig(url: String,
                          user: String,
                          pass: String,
                          connectionTimeout: FiniteDuration,
                          minConnections: Int,
                          maxConnections: Int)

@derive(pureconfigReader, loggable)
final case class HttpAppConfig(host: String, port: Int)

object AppConfig extends AppConfigCompanion[AppConfig]