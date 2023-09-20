name := "skimlinks-lambda"

organization := "com.gu"

description:= "A lambda function to periodically update a file in S3 with a list of domains supported by skimlinks.com, fetched from the skimlinks API."

version := "1.0"

scalaVersion := "2.12.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-target:jvm-1.8",
  "-Ywarn-dead-code"
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.12.420"
)

val circeVersion = "0.9.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

assembly / assemblyMergeStrategy := {
  case PathList(ps @ _*) if ps.last == "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

assemblyJarName := s"${name.value}.jar"
