package fi.spectrumlabs.db

import cats.{FlatMap, Functor}
import cats.tagless.syntax.functorK._
import derevo.derive
import doobie.ConnectionIO
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.derived.representableK
import tofu.logging.Logs
import tofu.syntax.monadic._

@derive(representableK)
trait MetadataRepository[F[_]] {
  def getMetadata(key: String): F[Option[String]]

}

object MetadataRepository {
  def make[I[_]: Functor, D[_]: FlatMap: LiftConnectionIO](
      implicit
      elh: EmbeddableLogHandler[D],
      logs: Logs[I, D]): I[MetadataRepository[D]] =
    logs.forService[MetadataRepository[D]].map { implicit __ =>
      elh.embed { implicit lh =>
        new RepoImpl(new MetadataSql).mapK(LiftConnectionIO[D].liftF)
      }
    }

  final class RepoImpl(sql: MetadataSql)
      extends MetadataRepository[ConnectionIO] {
    def getMetadata(key: String): ConnectionIO[Option[String]] = {
      sql.getMetadata(key).option
    }
  }
}
