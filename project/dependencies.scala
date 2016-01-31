import sbt._

object Dependencies {

  lazy val utils = common ++ serialization

  val common = Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe" % "config" % "1.3.0",
    "com.github.scopt" %% "scopt" % "3.3.0"
  )

  val serialization = Seq (
    "com.google.code.gson" % "gson" % "2.5"
  )

}
