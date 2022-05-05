package fi.spectrumlabs

import cats.effect.{Blocker, Resource}
import org.http4s.server.Server
import sttp.tapir.server.http4s.Http4sServerOptions
import tofu.doobie.log.EmbeddableLogHandler
import tofu.doobie.transactor.Txr
import tofu.lift.{IsoK, Unlift}
import tofu.logging.Logs
import tofu.logging.derivation.loggable.generate
import zio.interop.catz._
import zio.{ExitCode, URIO, ZIO}
import fi.spectrumlabs.db.MetadataRepository
import fi.spectrumlabs.http.MetadataService
import fi.spectrumlabs.http.HttpServer

object App extends EnvApp[AppContext] {

  implicit val serverOptions: Http4sServerOptions[RunF, RunF] = Http4sServerOptions.default[RunF, RunF]

  def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    init(args.headOption).use(_ => ZIO.never).orDie

  def init(configPathOpt: Option[String]): Resource[InitF, Server] =
    for {
      blocker <- Blocker[InitF]
      configs <- Resource.eval(AppConfig.load[InitF](configPathOpt, blocker))
      ctx                                = AppContext.init
      implicit0(ul: Unlift[RunF, InitF]) = Unlift.byIso(IsoK.byFunK(wr.runContextK(ctx))(wr.liftF))
      trans <- PostgresTransactor.make[InitF]("meta-db-pool", configs.pg)
      implicit0(xa: Txr.Continuational[RunF]) = Txr.continuational[RunF](trans.mapK(wr.liftF))
      implicit0(elh: EmbeddableLogHandler[xa.DB]) <-
        Resource.eval(doobieLogging.makeEmbeddableHandler[InitF, RunF, xa.DB]("meta-db-logging"))
      implicit0(logsDb: Logs[InitF, xa.DB]) = Logs.sync[InitF, xa.DB]
      implicit0(txReps: MetadataRepository[xa.DB]) <- Resource.eval(MetadataRepository.make[InitF, xa.DB])
      implicit0(m: MetadataService[RunF]) = MetadataService.make[RunF, xa.DB]
      server <- HttpServer.make[InitF, RunF](configs, runtime.platform.executor.asEC)
    } yield server
}