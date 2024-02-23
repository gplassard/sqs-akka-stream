import com.typesafe.sbt.packager.docker.ExecCmd

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.0"

val AkkaVersion = "2.8.5"
val AwsVersion = "2.24.7"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    dockerBaseImage := "eclipse-temurin:21-jdk-jammy",
    bashScriptExtraDefines ++= Seq("JAVA_OPTS=-javaagent:dd-java-agent.jar"),
    dockerCommands := dockerCommands.value.take(7) ++ Seq(
      ExecCmd("RUN", "curl", "-Lo", "/2/opt/docker/dd-java-agent.jar", "https://dtdg.co/latest-java-tracer")
    ) ++ dockerCommands.value.drop(7),
    name := "sqs-akka-stream",
    idePackagePrefix := Some("fr.gplassard.sqsakkastream"),
    libraryDependencies := Seq(
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "software.amazon.awssdk" % "sqs" % AwsVersion,
      "software.amazon.awssdk" % "sso" % AwsVersion,
    )
  )
