import NativePackagerHelper._
enablePlugins(GitVersioning)

val commonSettings = Seq(
  organization := "org.scardiecat",
  version := "0.0.4",
  scalaVersion := "2.11.7",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:existentials", "-language:higherKinds"),

  // build info
  buildInfoPackage := "meta",
  buildInfoOptions += BuildInfoOption.ToJson,
  buildInfoOptions += BuildInfoOption.BuildTime,
  buildInfoKeys := Seq[BuildInfoKey](
    name, version, scalaVersion
  ),
  publishMavenStyle := true,
  bintrayReleaseOnPublish in ThisBuild := true,
  bintrayPackageLabels := Seq("styx", "scala"),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
)

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
  .settings(
    name := """styx-utils""",
    libraryDependencies ++= Dependencies.utils,
    commonSettings
  )
