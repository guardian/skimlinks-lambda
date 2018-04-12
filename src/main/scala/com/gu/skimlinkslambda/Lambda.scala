package com.gu.skimlinkslambda

import com.amazonaws.services.lambda.runtime.Context
import org.slf4j.{ Logger, LoggerFactory }

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

case class Config(app: String, stack: String, stage: String, skimlinksApiKey: String, skimlinksAccountId: String,
  bucket: String, domainsKey: String) {
  override def toString: String =
    s"App: $app, Stack: $stack, Stage: $stage apikey: $skimlinksApiKey accountid: $skimlinksAccountId bucket: $bucket, key: $domainsKey \n"
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
      domainsBucket <- Option(System.getenv("DomainsBucket"))
      domainsKey <- Option(System.getenv("DomainsKey"))
    } yield {
      Config(app, stack, stage, apiKey, accountId, domainsBucket, domainsKey)
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

  def process(config: Config): Unit = {
    logger.info(s"Fetching skimlinks domains with config $config")
    val domains = SkimlinksAPI.getDomains(config.skimlinksApiKey, config.skimlinksAccountId)
    if (domains.isEmpty) {
      logger.error("Failed to fetch domains from skimlinks api")
      System.exit(1)
    } else {
      logger.info(s"Uploading ${domains.length} domains to S3://${config.bucket}/${config.domainsKey}")
      S3.uploadDomainsToS3(domains, config.bucket)
    }
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    args.foreach(println)
    if (args.length < 4) {
      println("Usage: run <apikey> <accountid> <bucket> <key>")
    } else {
      Lambda.process(Config("test", "test", "test", args(0), args(1), args(2), args(3)))
    }
  }
}
