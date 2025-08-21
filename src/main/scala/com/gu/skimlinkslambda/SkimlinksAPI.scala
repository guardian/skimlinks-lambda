package com.gu.skimlinkslambda

import io.circe.parser._
import org.slf4j.{Logger, LoggerFactory}
import scalaj.http._

object SkimlinksAPI {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def getAccessToken(clientId: String, clientSecret: String): Option[String] = {
    logger.info(s"Starting fetch of auth ${clientId}")
    val authResponse = Http(
      url = "https://authentication.skimapis.com/access_token",
    ).postData(
      s"""{
         | "client_id": "$clientId",
         | "client_secret": "$clientSecret",
         | "grant_type": "client_credentials"
         |}""".stripMargin
    ).headers(Map("Content-Type" -> "application/json")).asString

    if (authResponse.isSuccess) {
      parse(authResponse.body).map { authJson =>
        authJson.hcursor.
          downField("access_token").as[String]
      }.flatMap { result =>
        result
      }.toOption
    } else {
      logger.error(s"Could not obtain authentication token: $authResponse")
      None
    }
  }

  def getDomains(accessToken: String, publisherId: String): List[String] = {
    logger.info(s"calling get domains")
    val skimLinksDomainsUrl: String = s"https://merchants.skimapis.com/v4/publisher/$publisherId/domains"

    logger.info(s"access_token: ${accessToken.slice(0,5)}...")
    val domainsJson = Http(skimLinksDomainsUrl)
      .param("access_token", accessToken)
      .asString

    if (domainsJson.isSuccess) {
      logger.info(s"Success - ${domainsJson.code} response from $skimLinksDomainsUrl")
      parse(domainsJson.body).map { parsedJson =>
        parsedJson.hcursor.
          downField("domains").
          focus.
          flatMap(_.asArray).
          getOrElse(Vector.empty)
          .flatMap(_.hcursor.get[String]("domain").toOption)
          .toList
      }.getOrElse(List())
    } else {
      logger.error(s"Failed to reach skimlinks api - error ${domainsJson.code} ${domainsJson.statusLine}")
      List()
    }
  }
}