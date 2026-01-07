package com.gu.skimlinkslambda

import software.amazon.awssdk.auth.credentials.{ AwsCredentialsProviderChain, DefaultCredentialsProvider, ProfileCredentialsProvider }
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

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
      PutObjectRequest.builder().bucket(bucket).key("skimlinks/skimlinks-domains.csv").build(),
      RequestBody.fromString(domains.mkString(",")))

    println(result)
  }
}