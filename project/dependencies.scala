import sbt._

object Dependencies {

  lazy val utils = common

  val common = Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "co.blocke" % "scalajack_2.11" % "4.1"
  )

}
