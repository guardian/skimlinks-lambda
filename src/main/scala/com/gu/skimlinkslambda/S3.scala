package com.gu.skimlinkslambda

import software.amazon.awssdk.auth.credentials.{ AwsCredentialsProviderChain, DefaultCredentialsProvider, ProfileCredentialsProvider }
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{ GetObjectRequest, NoSuchKeyException, PutObjectRequest }

import scala.util.{ Try, Using }

object S3 {
  def uploadDomainsToS3(domains: List[String], bucket: String): Unit = {
    val credentials: AwsCredentialsProviderChain =
      AwsCredentialsProviderChain
        .builder()
        .credentialsProviders(
          ProfileCredentialsProvider.create("frontend"),
          DefaultCredentialsProvider.builder().build())
        .build()

    val s3Client = S3Client.builder().credentialsProvider(credentials).build()

    val result = s3Client.putObject(
      PutObjectRequest.builder()
        .bucket(bucket)
        .key("skimlinks/skimlinks-domains.csv")
        .build(),
      RequestBody.fromString(domains.mkString(",")))

    println(result)
  }

  def fetchPreviousDomainsFromS3(bucket: String): Option[List[String]] = {

    val credentials: AwsCredentialsProviderChain =
      AwsCredentialsProviderChain
        .builder()
        .credentialsProviders(
          ProfileCredentialsProvider.create("frontend"),
          DefaultCredentialsProvider.builder().build())
        .build()

    val s3Client = S3Client.builder().credentialsProvider(credentials).build()

    val request = GetObjectRequest.builder()
      .bucket(bucket)
      .key("skimlinks/skimlinks-domains.csv")
      .build()

    Try {
      Using.resource(s3Client.getObject(request)) { inputStream =>
        val content = scala.io.Source.fromInputStream(inputStream)
          .getLines()
          .mkString

        if (content.trim.isEmpty) {
          List.empty[String]
        } else {
          content.split(",").map(_.trim).filter(_.nonEmpty).toList
        }
      }
    }.recover {
      case _: NoSuchKeyException =>
        return None
      case e: Exception =>
        throw new RuntimeException("Failed to fetch previous domains from S3", e)
    }.toOption
  }
}