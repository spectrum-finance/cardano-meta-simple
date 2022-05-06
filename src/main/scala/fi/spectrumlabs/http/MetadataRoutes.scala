package fi.spectrumlabs.http

import cats.effect.{Concurrent, ContextShift, Timer}
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import cats.syntax.functor._
import io.circe.parser._

final class MetadataRoutes[F[_]: Concurrent: ContextShift: Timer](
    implicit
    service: MetadataService[F],
    opts: Http4sServerOptions[F, F]) {

  private val endpoints = MetadataEndpoint
  import endpoints._

  private val interpreter = Http4sServerInterpreter(opts)

  def routes = getMetadataR

  def getMetadataR = interpreter.toRoutes(getMetadata) { key =>
    service.getMeta(key).map(_.flatMap(r => parse(r).toOption)).orNotFound(s"Meta key $key is not found.")
  }

}

object MetadataRoutes {
  def make[F[_]: Concurrent: ContextShift: Timer](
      implicit
      service: MetadataService[F],
      opts: Http4sServerOptions[F, F]): HttpRoutes[F] =
    new MetadataRoutes[F]().routes
}
