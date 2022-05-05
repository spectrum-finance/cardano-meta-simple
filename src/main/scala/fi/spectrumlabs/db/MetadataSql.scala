package fi.spectrumlabs.db

import doobie._
import doobie.syntax.all._
import doobie.util.fragment

final class MetadataSql(implicit lh: LogHandler) {

  def getMetadata(key: String): Query0[String] =
    (fr"select value from metadata where key = " ++ Fragment.const("'" ++ key ++ "'")) .query[String]
}
