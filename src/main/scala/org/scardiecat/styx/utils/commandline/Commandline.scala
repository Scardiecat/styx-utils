package org.scardiecat.styx.utils.commandline

case class Commandline(roles: Seq[String] = Seq()
                    , seeds: Seq[String] = Seq()
                    , port: Int = Int.MaxValue
                    , actorSystemName: String ="")
