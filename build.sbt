name := "waivers"

organization := "io.bryzek"

scalaVersion := "3.6.4"

version := "0.0.1"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      jdbc,
      evolutions,
      "org.postgresql" % "postgresql" % "42.7.4",
      "org.playframework.anorm" %% "anorm" % "2.7.0",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "com.typesafe.play" %% "play-json" % "2.10.6",
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
      
      // Email support
      "com.typesafe.play" %% "play-mailer" % "10.0.0",
      "com.typesafe.play" %% "play-mailer-guice" % "10.0.0",
      
      // HTTP client for SignNow integration
      "com.softwaremill.sttp.client3" %% "core" % "3.9.8",
      "com.softwaremill.sttp.client3" %% "play-json" % "3.9.8",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-future" % "3.9.8"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-Werror",
      "-Wunused:all",
      "-deprecation"
    )
  )

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)