package com.gu.skimlinkslambda

import io.circe.parser._
import org.slf4j.{ Logger, LoggerFactory }
import scalaj.http._

object SkimlinksAPI {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def getAccessToken(clientId: String, clientSecret: String): Option[String] = {
    logger.info(s"calling getAccessToken for clientId: ${clientId.take(4)}****")
    val authResponse = Http(
      url = "https://authentication.skimapis.com/access_token").postData(
      s"""{
         | "client_id": "$clientId",
         | "client_secret": "$clientSecret",
         | "grant_type": "client_credentials"
         |}""".stripMargin).headers(Map("Content-Type" -> "application/json")).asString

    if (authResponse.isSuccess) {
      logger.info("Successfully received response from Skimlinks authentication API")
      parse(authResponse.body).map { authJson =>
        authJson.hcursor.
          downField("access_token").as[String]
      }.flatMap { result =>
        result
      }.toOption
    } else {
      logger.error(s"Could not obtain authentication token. Status: ${authResponse.code}, Body: ${authResponse.body}")
      None
    }
  }

  def getDomains(accessToken: String, publisherId: String): List[String] = {
    logger.info(s"Fetching domains for publisherId: $publisherId")
    val skimLinksDomainsUrl: String = s"https://merchants.skimapis.com/v4/publisher/$publisherId/domains"

    logger.info(s"get domains using access_token: ${accessToken.slice(0, 4)}****")
    val domainsJson = Http(skimLinksDomainsUrl)
      .param("access_token", accessToken)
      .timeout(connTimeoutMs = 10000, readTimeoutMs = 30000)
      .asString

    if (domainsJson.isSuccess) {
      logger.info(s"Success - ${domainsJson.code} response from $skimLinksDomainsUrl")
      parse(domainsJson.body).map { parsedJson =>
        val domains = parsedJson.hcursor.
          downField("domains").
          focus.
          flatMap(_.asArray).
          getOrElse(Vector.empty)
          .flatMap(_.hcursor.get[String]("domain").toOption)
          .toList
        logger.info(s"Parsed ${domains.length} domains. Sample: ${domains.take(5).mkString(", ")}")
        domains
      }.getOrElse {
        logger.error("Failed to parse domains from response body")
        List()
      }
    } else {
      logger.error(s"Failed to reach Skimlinks API - error ${domainsJson.code} ${domainsJson.statusLine}")
      List()
    }
  }
}