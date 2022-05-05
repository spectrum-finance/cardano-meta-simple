package fi.spectrumlabs.http

import fi.spectrumlabs.db.MetadataRepository

import cats.Monad
import cats.syntax.option._
import mouse.anyf._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr
import tofu.syntax.monadic._

trait MetadataService[F[_]] {
  def getMeta(key: String): F[Option[String]]
}

object MetadataService {

  def make[F[_], D[_]: Monad: LiftConnectionIO](
      implicit
      txr: Txr[F, D],
      repo: MetadataRepository[D]): MetadataService[F] =
    new Impl[F, D](txr, repo)

  final class Impl[F[_], D[_]: Monad](txr: Txr[F, D],
                                      repo: MetadataRepository[D])
      extends MetadataService[F] {
    def getMeta(key: String): F[Option[String]] =
      repo.getMetadata(key) ||> txr.trans
  }
}
