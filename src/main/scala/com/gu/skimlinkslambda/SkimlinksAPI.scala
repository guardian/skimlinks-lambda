package com.gu.skimlinkslambda

import scalaj.http._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.slf4j.{ Logger, LoggerFactory }

case class Domain(domain: String)
case class SkimlinksDomains(has_more: Boolean, domains: List[Domain])

object SkimlinksAPI {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def getDomains(apiKey: String, accountId: String): Either[Error, List[String]] = {
    val skimLinksDomainsUrl: String = s"https://merchants.skimapis.com/v3/domains"

    val domainsJson: String = Http(skimLinksDomainsUrl)
      .param("apikey", apiKey)
      .param("account_type", "publisher_admin")
      .param("account_id", accountId)
      .asString.body

    val decodedDomains: Either[Error, SkimlinksDomains] = decode[SkimlinksDomains](domainsJson)

    decodedDomains.map { domains =>
      if (domains.has_more) {
        logger.warn(s"All domains not fetched! Fetched ${domains.domains.length} domains")
      }
      domains.domains.map(_.domain)
    }
  }
}