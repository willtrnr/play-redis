val commonSettings: Seq[Def.Setting[_]] = Seq(
  organization := "net.archwill.play",

  name := "play-redis",

  scalaVersion := "2.11.11",
  crossScalaVersions := Seq("2.11.11"),

  libraryDependencies ++= Seq(
    "javax.inject" % "javax.inject" % "1",
    "redis.clients" % "jedis" % "3.0.1",
    "org.apache.commons" % "commons-pool2" % "2.4.3",
  ),

  releaseCrossBuild := true,
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    publishArtifact := false,
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

    crossScalaVersions += "2.12.8",

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.1",
      "com.typesafe.play" %% "play" % "2.6.0" % "provided",
      "com.typesafe.play" %% "play-cache" % "2.6.0" % "provided",
      "com.typesafe.akka" %% "akka-actor" % "2.5.3" % "provided",
      "com.jsuereth" %% "scala-arm" % "2.0",
    ),

    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src_play26+" / "main" / "java",
      baseDirectory.value / "src_play26+" / "main" / "scala",
    ),

    target := baseDirectory.value / "target" / "play26",
  )

lazy val play27 = (project in file("./play-redis"))
  .settings(commonSettings)
  .settings(
    version := addVerSuffix(version.value, "play27"),

    crossScalaVersions += "2.12.8",

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.3",
      "com.typesafe.play" %% "play" % "2.7.0" % "provided",
      "com.typesafe.play" %% "play-cache" % "2.7.0" % "provided",
      "com.typesafe.akka" %% "akka-actor" % "2.5.19" % "provided",
      "com.jsuereth" %% "scala-arm" % "2.0",
    ),

    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "src_play26+" / "main" / "java",
      baseDirectory.value / "src_play26+" / "main" / "scala",
    ),

    target := baseDirectory.value / "target" / "play27",
  )

def addVerSuffix(version: String, suffix: String): String = version.split('-') match {
  case Array(v, "SNAPSHOT") => s"$v.$suffix-SNAPSHOT"
  case _ => s"$version.$suffix"
}
