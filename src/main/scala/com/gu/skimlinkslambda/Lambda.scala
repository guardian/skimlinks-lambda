package com.gu.skimlinkslambda

import com.amazonaws.services.lambda.runtime.Context
import org.slf4j.{ Logger, LoggerFactory }
import com.gu.skimlinkslambda.S3
import com.gu.skimlinkslambda.SkimlinksAPI

/**
 * This is compatible with aws' lambda JSON to POJO conversion.
 * You can test your lambda by sending it the following payload:
 * {"name": "Bob"}
 */
class LambdaInput() {
  var name: String = _

  def getName(): String = name

  def setName(theName: String): Unit = name = theName
}

case class Config(
  app: String,
  stack: String,
  stage: String,
  skimlinksApiKey: String,
  skimlinksAccountId: String,
  skimlinksClientId: String,
  skimlinksClientSecret: String,
  bucket: String,
  domainsKey: String) {
  override def toString: String =
    s"App: $app, " +
      s"Stack: $stack, " +
      s"Stage: $stage, " +
      s"apikey: $skimlinksApiKey, " +
      s"accountId: $skimlinksAccountId, " +
      s"clientId: ${skimlinksClientId.head}****, " +
      s"clientSecret: ${skimlinksClientSecret.head}****, " +
      s"bucket: $bucket, " +
      s"domainsKey: $domainsKey \n"
}

object Lambda {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def configFromEnvironmentVariables: Option[Config] = {
    for {
      app <- Option(System.getenv("App"))
      stack <- Option(System.getenv("Stack"))
      stage <- Option(System.getenv("Stage"))
      apiKey <- Option(System.getenv("SkimlinksApiKey"))
      accountId <- Option(System.getenv("SkimlinksAccountId"))
      clientId <- Option(System.getenv("SkimlinksClientId"))
      clientSecret <- Option(System.getenv("SkimlinksClientSecret"))
      domainsBucket <- Option(System.getenv("DomainsBucket"))
      domainsKey <- Option(System.getenv("DomainsKey"))
    } yield {
      Config(
        app = app,
        stack = stack,
        stage = stage,
        skimlinksApiKey = apiKey,
        skimlinksAccountId = accountId,
        skimlinksClientId = clientId,
        skimlinksClientSecret = clientSecret,
        bucket = domainsBucket,
        domainsKey = domainsKey)
    }
  }

  /*
   * This is your lambda entry point
   */
  def handler(lambdaInput: LambdaInput, context: Context): Unit = {
    val config = configFromEnvironmentVariables
    if (config.isDefined) {
      process(config.get)
    } else {
      logger.error("Missing or incorrect config. Please check environment variables.")
      System.exit(1)
    }
  }

  def hasExcessiveDropOff(previousCount: Int, newCount: Int): Boolean = {
    previousCount > 0 && newCount < previousCount * 0.8
  }

  def process(config: Config): Unit = {
    logger.info(s"Fetching the skimlinks domains with config $config")

    val domains = SkimlinksAPI.getAccessToken(config.skimlinksClientId, config.skimlinksClientSecret) match {
      case Some(authToken) => SkimlinksAPI.getDomains(authToken, config.skimlinksAccountId)
      case None =>
        logger.error("Failed to obtain access token from Skimlinks API")
        List.empty
    }

    if (domains.isEmpty) {
      logger.error("Skimlinks API returned an empty domain list")
      System.exit(1)
    }

    val previousDomains = S3.fetchPreviousDomainsFromS3(config.bucket) match {
      case Some(prev) =>
        logger.info(s"Fetched ${prev.length} previous domains from S3")
        prev
      case None =>
        logger.warn("No previous domains found in S3, this might be the first run")
        List.empty
    }

    if (previousDomains.nonEmpty && hasExcessiveDropOff(previousDomains.length, domains.length)) {
      val dropPercent = ((previousDomains.length - domains.length).toDouble / previousDomains.length * 100).round
      logger.warn(s"Domain count dropped from ${previousDomains.length} to ${domains.length} ($dropPercent% drop), exceeding 20% threshold. Consider investigating and raising with Skimlinks.")
    }

    logger.info(s"Uploading ${domains.length} domains to S3://${config.bucket}/${config.domainsKey}")
    S3.uploadDomainsToS3(domains, config.bucket)
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    args.foreach(println)
    if (args.length < 6) {
      println("Usage: run <apikey> <accountId> <clientId> <clientSecret> <domainsBucket> <domainsKey>")
    } else {
      Lambda.process(Config("test", "test", "test", args(0), args(1), args(2), args(3), args(4), args(5)))
    }
  }
}
