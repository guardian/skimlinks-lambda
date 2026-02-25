name := "skimlinks-lambda"

organization := "com.gu"

description:= "A lambda function to periodically update a file in S3 with a list of domains supported by skimlinks.com, fetched from the skimlinks API."

version := "1.0"

scalaVersion := "2.13.18"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-release:11",
  "-Ywarn-dead-code"
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.4.0",
  "org.slf4j" % "slf4j-simple" % "2.0.17",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "software.amazon.awssdk" % "s3" % "2.41.26",
)

val circeVersion = "0.14.15"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.21.1"

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}

assemblyJarName := s"${name.value}.jar"
