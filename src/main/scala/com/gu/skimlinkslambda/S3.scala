package com.gu.skimlinkslambda

import com.amazonaws.auth.{ AWSCredentialsProvider, AWSCredentialsProviderChain, DefaultAWSCredentialsProviderChain }
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }
import com.amazonaws.services.s3.model.PutObjectResult

object S3 {
  def uploadDomainsToS3(domains: List[String], bucket: String): Unit = {
    val credentialsProvider = new AWSCredentialsProviderChain(new ProfileCredentialsProvider("frontend"), new DefaultAWSCredentialsProviderChain)
    val s3Client = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).build()
    val result: PutObjectResult = s3Client.putObject(bucket, "skimlinks/skimlinks-domains.csv", domains.mkString(","))
    println(result)
  }
}