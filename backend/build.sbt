name := "waivers-backend"

organization := "io.bryzek"

ThisBuild / scalaVersion := "3.6.4"

ThisBuild / javacOptions ++= Seq("-source", "17", "-target", "17")

lazy val allScalacOptions = Seq(
  "-feature",
  "-Xfatal-warnings",
  "-Wunused:locals",
  "-Wunused:params",
  "-Wimplausible-patterns"
)

lazy val resolversSettings = Seq(
  resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  resolvers += "jitpack" at "https://jitpack.io",
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

lazy val commonSettings: Seq[Setting[_]] = Seq(
  name ~= ("waivers-" + _),
  Compile / doc / sources := Seq.empty,
  Compile / packageDoc / publishArtifact := false,
  testOptions += Tests.Argument("-oDF"),
  libraryDependencies ++= Seq(
    jdbc,
  )
)

lazy val generated = project
  .in(file("generated"))
  .enablePlugins(PlayScala)
  .settings(resolversSettings)
  .settings(commonSettings)
  .settings(
    scalacOptions ++= Seq(
      "-deprecation:false"
    ),
    libraryDependencies ++= Seq(
      ws,
      "com.github.mbryzek" % "lib-query" % "0.0.14",
      "com.github.mbryzek" % "lib-util" % "0.0.10",
      "joda-time" % "joda-time" % "2.13.1",
      "org.playframework.anorm" %% "anorm-postgres" % "2.7.0",
      "org.postgresql" % "postgresql" % "42.7.5",
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
    ),
  )

lazy val root = (project in file("."))
  .dependsOn(generated % "compile->compile;test->test")
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(resolversSettings)
  .settings(
    PlayKeys.fileWatchService := play.dev.filewatch.FileWatchService.jdk7(play.sbt.run.toLoggerProxy(sLog.value)),
    PlayKeys.playDefaultPort := 9300,
    Compile / doc / sources := Seq.empty,
    Compile / packageDoc / publishArtifact := false,
    testOptions += Tests.Argument("-oF"),
    routesGenerator := InjectedRoutesGenerator,
    Test / javaOptions += "-Dconfig.resource=test.conf",
    libraryDependencies ++= Seq(
      ws,
      jdbc,
      filters,
      guice,
      evolutions,
      ("com.github.mbryzek" % "lib-cipher" % "0.0.7").cross(CrossVersion.for3Use2_13),
      "com.typesafe.play" %% "play-guice" % "2.9.6",
      "com.google.inject" % "guice" % "5.1.0",
      "com.google.inject.extensions" % "guice-assistedinject" % "5.1.0",
      "org.postgresql" % "postgresql" % "42.7.5",
      "org.playframework.anorm" %% "anorm-postgres" % "2.7.0",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "com.typesafe.play" %% "play-json" % "2.10.6",
      "com.github.apicollective" % "apibuilder-validation" % "0.5.2",
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
      
      // Email support (using cross-compiled versions for Scala 3)
      ("com.typesafe.play" %% "play-mailer" % "9.0.0").cross(CrossVersion.for3Use2_13),
      ("com.typesafe.play" %% "play-mailer-guice" % "9.0.0").cross(CrossVersion.for3Use2_13),
      
      // HTTP client for HelloSign integration
      "com.softwaremill.sttp.client3" %% "core" % "3.9.8",
      "com.softwaremill.sttp.client3" %% "play-json" % "3.9.8",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-future" % "3.9.8"
    ),
    scalacOptions ++= allScalacOptions,
    Test / scalacOptions ~= { opts => opts.filterNot(_ == "-Xfatal-warnings") },
    Test / javaOptions ++= Seq(
      "--add-exports=java.base/sun.security.x509=ALL-UNNAMED",
      "--add-opens=java.base/sun.security.ssl=ALL-UNNAMED"
   ),
  )
