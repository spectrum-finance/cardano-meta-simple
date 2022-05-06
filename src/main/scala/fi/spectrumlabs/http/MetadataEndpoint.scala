package fi.spectrumlabs.http

import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import fi.spectrumlabs.http.HttpError
import cats.syntax.option._
import io.circe.Json
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object MetadataEndpoint {

  val prefix = "metadata"

  def endpoints: List[Endpoint[_, _, _, _]] = getMetadata :: Nil

  def getMetadata: Endpoint[String, HttpError, Json, Any] =
    endpoint.get
      .in(prefix / path[String].description("metadata key"))
      .out(jsonBody[Json])
      .tag(prefix)
      .name("Metadata by key")
      .errorOut(
        oneOf[HttpError](
          oneOfMapping(StatusCode.NotFound,
                       jsonBody[HttpError.NotFound].description("not found")),
          oneOfMapping(StatusCode.NoContent,
                       emptyOutputAs(HttpError.NoContent)),
          oneOfDefaultMapping(
            jsonBody[HttpError.Unknown].description("unknown"))
        )
      )

}
