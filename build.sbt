val scala3Version = "3.5.1"
lazy val akkaVersion = "2.8.5"
lazy val akkaHttpVersion = "10.5.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "hello-world",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.play" %% "play-json" % "2.10.3",
      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "org.scalameta" %% "munit" % "1.0.0" % Test
    )
  )
