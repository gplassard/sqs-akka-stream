ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.0"

lazy val root = (project in file("."))
  .settings(
    name := "sqs-akka-stream",
    idePackagePrefix := Some("fr.gplassard.sqsakkastream")
  )
