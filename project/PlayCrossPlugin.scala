import sbt._
import sbt.Keys._

object PlayCrossKeys {

  val playMinorVersionRange = settingKey[Range]("Play minor version range to use for cross source directories.")

}

object PlayCrossPlugin extends AutoPlugin {

  override def requires = plugins.JvmPlugin

  override def projectSettings: Seq[Def.Setting[_]] = playCrossProjectSettings

  val autoImport = PlayCrossKeys

  import autoImport._

  def playCrossProjectSettings: Seq[Def.Setting[_]] = Seq(
    playMinorVersionRange := 4 to 8,

    version := playVersionSuffix.value.fold(version.value)(addVerSuffix(version.value, _)),
    unmanagedSourceDirectories in Compile ++= playCrossSources.value,
    target := playVersionSuffix.value.fold(target.value)(baseDirectory.value / "target" / _),
  )

  def addVerSuffix(version: String, suffix: String): String =
    version.split('-') match {
      case Array(v, "SNAPSHOT") => s"$v.$suffix-SNAPSHOT"
      case _ => s"$version.$suffix"
    }

  def playVersion: Def.Initialize[Option[String]] = Def.setting {
    libraryDependencies.value
      .find(m => m.organization == "com.typesafe.play" && m.name == "play")
      .map(_.revision)
  }

  def playVersionSuffix: Def.Initialize[Option[String]] = Def.setting {
    playVersion.value flatMap {
      _.split('.') match {
        case Array(major, minor, _) =>
          Some(s"play${major}${minor}")
        case _ =>
          None
      }
    }
  }

  def playCrossSources: Def.Initialize[Seq[File]] = Def.setting {
    playVersion.value.fold(Seq.empty[File]) { v =>
      v.split('.') match {
        case Array("2", minor, _) =>
          for {
            i <- playMinorVersionRange.value.toSeq
            d <- (
              (if (i == minor.toInt) Seq(s"src_play2${i}") else Seq.empty) ++
              (if (i >= minor.toInt) Seq(s"src_play2${i}-") else Seq.empty) ++
              (if (i <= minor.toInt) Seq(s"src_play2${i}+") else Seq.empty)
            )
            l <- Seq("java", "scala")
          } yield {
            baseDirectory.value / d / "main" / l
          }
        case _ =>
          Seq.empty
      }
    }
  }

}
