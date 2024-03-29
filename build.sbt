import ReleaseTransformations._

val scala211 = "2.11.11"
val scala212 = "2.12.8"

val commonSettings: Seq[Def.Setting[_]] = Seq(
  organization := "net.archwill.play",
  version := (version in ThisBuild).value,

  name := "play-redis",
  description := "Redis cache for Play Framework",

  homepage := Some(url("https://github.com/willtrnr/play-redis")),

  licenses := Seq(("MIT", url("http://opensource.org/licenses/MIT"))),

  scmInfo := Some(ScmInfo(
    url("https://github.com/willtrnr/play-redis"),
    "scm:git:https://github.com/willtrnr/play-redis.git",
    "scm:git:ssh://git@github.com/willtrnr/play-redis.git"
  )),

  publishTo := Some("GitHub" at "https://maven.pkg.github.com/willtrnr/maven-repo"),

  scalaVersion := scala211,
  crossScalaVersions := Seq(scala211),

  libraryDependencies ++= Seq(
    "javax.inject" % "javax.inject" % "1",
    "com.google.guava" % "guava" % "28.0-jre",
    "org.apache.commons" % "commons-pool2" % "2.4.3",
    "redis.clients" % "jedis" % "3.1.0"
  ),

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

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    libraryDependencies := Seq.empty,

    publishArtifact := false,
    skip in publish := true,
    publish := {},
  )
  .aggregate(play24, play25, play26, play27)

lazy val play24 = (project in file("./play-redis"))
  .settings(commonSettings)
  .settings(
    version := addVerSuffix(version.value, "play24"),

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.0",
      "com.typesafe.play" %% "play" % "2.4.0" % "provided",
      "com.typesafe.play" %% "play-cache" % "2.4.0" % "provided" exclude("net.sf.ehcache", "ehcache-core"),
      "com.typesafe.akka" %% "akka-actor" % "2.3.11" % "provided",
      "com.jsuereth" %% "scala-arm" % "1.4",
    ),

    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src_play25-" / "main" / "java",
      baseDirectory.value / "src_play25-" / "main" / "scala",
    ),

    target := baseDirectory.value / "target" / "play24",
  )

lazy val play25 = (project in file("./play-redis"))
  .settings(commonSettings)
  .settings(
    version := addVerSuffix(version.value, "play25"),

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.0",
      "com.typesafe.play" %% "play" % "2.5.0" % "provided",
      "com.typesafe.play" %% "play-cache" % "2.5.0" % "provided" exclude("net.sf.ehcache", "ehcache-core"),
      "com.typesafe.akka" %% "akka-actor" % "2.4.2" % "provided",
      "com.jsuereth" %% "scala-arm" % "2.0",
    ),

    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src_play25-" / "main" / "java",
      baseDirectory.value / "src_play25-" / "main" / "scala",
    ),

    target := baseDirectory.value / "target" / "play25",
  )

lazy val play26 = (project in file("./play-redis"))
  .settings(commonSettings)
  .settings(
    version := addVerSuffix(version.value, "play26"),

    crossScalaVersions += scala212,

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.1",
      "com.typesafe.play" %% "play" % "2.6.0" % "provided",
      "com.typesafe.play" %% "play-cache" % "2.6.0" % "provided",
      "com.typesafe.akka" %% "akka-actor" % "2.5.3" % "provided",
      "com.jsuereth" %% "scala-arm" % "2.0",
    ),

    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src_play26_play27" / "main" / "java",
      baseDirectory.value / "src_play26_play27" / "main" / "scala",
    ),

    target := baseDirectory.value / "target" / "play26",
  )

lazy val play27 = (project in file("./play-redis"))
  .settings(commonSettings)
  .settings(
    version := addVerSuffix(version.value, "play27"),

    crossScalaVersions += scala212,

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.3",
      "com.typesafe.play" %% "play" % "2.7.0" % "provided",
      "com.typesafe.play" %% "play-cache" % "2.7.0" % "provided",
      "com.typesafe.akka" %% "akka-actor" % "2.5.19" % "provided",
      "com.jsuereth" %% "scala-arm" % "2.0",
    ),

    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src_play26_play27" / "main" / "java",
      baseDirectory.value / "src_play26_play27" / "main" / "scala",
    ),

    target := baseDirectory.value / "target" / "play27",
  )

lazy val play28 = (project in file("./play-redis"))
  .settings(commonSettings)
  .settings(
    version := addVerSuffix(version.value, "play28"),

    scalaVersion := scala212,

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.4",
      "com.typesafe.play" %% "play" % "2.8.0-M4" % "provided",
      "com.typesafe.play" %% "play-cache" % "2.8.0-M4" % "provided",
      "com.typesafe.akka" %% "akka-actor" % "2.6.0-M5" % "provided",
      "com.jsuereth" %% "scala-arm" % "2.0",
    ),

    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src_play28+" / "main" / "java",
      baseDirectory.value / "src_play28+" / "main" / "scala",
    ),

    target := baseDirectory.value / "target" / "play28",
  )

def addVerSuffix(version: String, suffix: String): String = version.split('-') match {
  case Array(v, "SNAPSHOT") => s"$v.$suffix-SNAPSHOT"
  case _ => s"$version.$suffix"
}
