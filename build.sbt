ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.0"

val AkkaVersion = "2.8.5"
val AwsVersion = "2.24.7"

lazy val root = (project in file("."))
  .settings(
    name := "sqs-akka-stream",
    idePackagePrefix := Some("fr.gplassard.sqsakkastream"),
    libraryDependencies := Seq(
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "software.amazon.awssdk" % "sqs" % AwsVersion,
      "software.amazon.awssdk" % "sso" % AwsVersion,
    )
  )
