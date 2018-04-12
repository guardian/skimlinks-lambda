package com.gu.skimlinkslambda

import scalaj.http._
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.slf4j.{ Logger, LoggerFactory }
import io.circe.optics.JsonPath._

object SkimlinksAPI {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def getDomains(apiKey: String, accountId: String): List[String] = {
    val skimLinksDomainsUrl: String = s"https://merchants.skimapis.com/v3/domains"

    val domainsJson = Http(skimLinksDomainsUrl)
      .param("apikey", apiKey)
      .param("account_type", "publisher_admin")
      .param("account_id", accountId)
      .asString

    if (domainsJson.isSuccess) {
      logger.info(s"Success - ${domainsJson.code} response from $skimLinksDomainsUrl")
      parse(domainsJson.body).map { parsedJson =>
        root.domains.each.domain.string.getAll(parsedJson)
      }.getOrElse(List())
    } else {
      logger.error(s"Failed to reach skimlinks api - error ${domainsJson.code} ${domainsJson.statusLine}")
      List()
    }
  }
}