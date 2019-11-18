import ReleaseTransformations._

val scala211 = "2.11.11"
val scala212 = "2.12.8"

val commonSettings: Seq[Def.Setting[_]] = Seq(
  organization := "net.archwill.play",

  name := "play-redis",

  licenses := Seq(("MIT", url("http://opensource.org/licenses/MIT"))),

  scalaVersion := scala211,
  crossScalaVersions := Seq(scala211),

  libraryDependencies ++= Seq(
    "javax.inject" % "javax.inject" % "1",
    "com.google.guava" % "guava" % "28.0-jre",
    "org.apache.commons" % "commons-pool2" % "2.4.3",
    "redis.clients" % "jedis" % "3.1.0"
  ),
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    libraryDependencies := Seq.empty,

    publishArtifact := false,
    skip in publish := true,
    publish := {},

    releaseCrossBuild := false,

    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommandAndRemaining("+test"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publish"),
      setNextVersion,
      commitNextVersion,
      pushChanges,
    ),
  )
  .aggregate(play24, play25, play26, play27)

lazy val play24 = (project in file("./play-redis"))
  .enablePlugins(PlayCrossPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.0",
      "com.typesafe.play" %% "play" % "2.4.0" % "provided",
      "com.typesafe.play" %% "play-cache" % "2.4.0" % "provided" exclude("net.sf.ehcache", "ehcache-core"),
      "com.typesafe.akka" %% "akka-actor" % "2.3.11" % "provided",
      "com.jsuereth" %% "scala-arm" % "1.4",
    ),
  )

lazy val play25 = (project in file("./play-redis"))
  .enablePlugins(PlayCrossPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.0",
      "com.typesafe.play" %% "play" % "2.5.0" % "provided",
      "com.typesafe.play" %% "play-cache" % "2.5.0" % "provided" exclude("net.sf.ehcache", "ehcache-core"),
      "com.typesafe.akka" %% "akka-actor" % "2.4.2" % "provided",
      "com.jsuereth" %% "scala-arm" % "2.0",
    ),
  )

lazy val play26 = (project in file("./play-redis"))
  .enablePlugins(PlayCrossPlugin)
  .settings(commonSettings)
  .settings(
    crossScalaVersions += scala212,

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.1",
      "com.typesafe.play" %% "play" % "2.6.0" % "provided",
      "com.typesafe.play" %% "play-cache" % "2.6.0" % "provided",
      "com.typesafe.akka" %% "akka-actor" % "2.5.3" % "provided",
      "com.jsuereth" %% "scala-arm" % "2.0",
    ),
  )

lazy val play27 = (project in file("./play-redis"))
  .enablePlugins(PlayCrossPlugin)
  .settings(commonSettings)
  .settings(
    crossScalaVersions += scala212,

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.3",
      "com.typesafe.play" %% "play" % "2.7.0" % "provided",
      "com.typesafe.play" %% "play-cache" % "2.7.0" % "provided",
      "com.typesafe.akka" %% "akka-actor" % "2.5.19" % "provided",
      "com.jsuereth" %% "scala-arm" % "2.0",
    ),
  )

lazy val play28 = (project in file("./play-redis"))
  .enablePlugins(PlayCrossPlugin)
  .settings(commonSettings)
  .settings(
    scalaVersion := scala212,
    crossScalaVersions := Seq(scala212),

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.4",
      "com.typesafe.play" %% "play" % "2.8.0-M4" % "provided",
      "com.typesafe.play" %% "play-cache" % "2.8.0-M4" % "provided",
      "com.typesafe.akka" %% "akka-actor" % "2.6.0-M5" % "provided",
      "com.jsuereth" %% "scala-arm" % "2.0",
    ),
  )
