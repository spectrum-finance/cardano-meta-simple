package fi.spectrumlabs

import cats.Applicative
import cats.syntax.either._
import tofu.Catches
import tofu.syntax.handle._
import tofu.syntax.monadic._
import org.http4s.HttpRoutes
import tofu.lift.Unlift
import cats.data.Kleisli
import cats.{~>, Functor, Monad}
import tofu.syntax.monadic._
import cats.data.OptionT
import tofu.lift.IsoK
import tofu.higherKind.Embed
import org.http4s._

package object http {
      implicit class ServiceOps[F[_], A](protected val fa: F[A]) extends AnyVal {

    def eject(implicit F: Applicative[F], C: Catches[F]): F[Either[HttpError, A]] =
      fa.map(_.asRight[HttpError]).handle[Throwable](e => HttpError.Unknown(500, e.getMessage).asLeft)
  }

  implicit class ServiceOptionOps[F[_], A](protected val fa: F[Option[A]]) extends AnyVal {

    def orNotFound(what: String)(implicit F: Applicative[F], C: Catches[F]): F[Either[HttpError, A]] =
      fa.map(_.fold[Either[HttpError, A]](HttpError.NotFound(what).asLeft)(_.asRight))
        .handle[Throwable](e => HttpError.Unknown(500, e.getMessage).asLeft)
  }

    def imapHttpApp[F[_], G[_]: Functor](app: HttpApp[F])(fk: F ~> G)(gK: G ~> F): HttpApp[G] =
    Kleisli(req => app.mapK(fk).run(req.mapK(gK)).map(_.mapK(fk)))

  def translateHttpApp[F[_], G[_]: Functor](app: HttpApp[F])(implicit FG: IsoK[F, G]): HttpApp[G] =
    imapHttpApp(app)(FG.tof)(FG.fromF)

  def imapRoutes[F[_], G[_]: Functor](routes: HttpRoutes[F])(fk: F ~> G)(gK: G ~> F): HttpRoutes[G] =
    Kleisli(greq => routes.run(greq.mapK(gK)).mapK(fk).map(_.mapK(fk)))

  def translateRoutes[F[_], G[_]: Functor](routes: HttpRoutes[F])(implicit FG: IsoK[F, G]): HttpRoutes[G] =
    imapRoutes(routes)(FG.tof)(FG.fromF)

  def unliftRoutes[F[_], G[_]: Monad](routes: HttpRoutes[F])(implicit FG: Unlift[F, G]): HttpRoutes[G] =
    Embed.of[Http[*[_], G], OptionT[G, *]](OptionT.liftF(FG.subIso.map(implicit iso => translateRoutes[F, G](routes))))

}
