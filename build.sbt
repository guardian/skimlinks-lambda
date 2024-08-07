name := "skimlinks-lambda"

organization := "com.gu"

description:= "A lambda function to periodically update a file in S3 with a list of domains supported by skimlinks.com, fetched from the skimlinks API."

version := "1.0"

scalaVersion := "2.13.12"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-release:11",
  "-Ywarn-dead-code"
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.2",
  "org.slf4j" % "slf4j-simple" % "2.0.5",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.12.643"
)

val circeVersion = "0.14.6"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "[2.16,)"

assembly / assemblyMergeStrategy := {
  case PathList(ps @ _*) if ps.last == "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

assemblyJarName := s"${name.value}.jar"
