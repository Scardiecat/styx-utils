package org.scardiecat.styx.utils.commandline

import com.typesafe.config.{ConfigFactory, Config}
import org.scardiecat.styx.utils.environment

object CommandlineParser {
  def parse(args:Array[String], appName:String, appConfig:Config, defaultRoles:Seq[String]= Seq[String]()): Commandline = {
    val parser = new scopt.OptionParser[Commandline](appName) {
      head(appName)
      opt[Seq[String]]('r', "roles") valueName("<role>,<role>...") action { (x,c) =>
        c.copy(roles = x) } text("roles")
      opt[Seq[String]]('s', "seeds") valueName("<seednode>,<seednode>...") optional() action { (x,c) =>
        c.copy(seeds = x) } text("seed nodes")
      opt[Int]('p', "port") action { (x, c) =>
        c.copy(port = x) } text("port the actor system listens too")
      opt[String]('n', "name") action { (x, c) =>
        c.copy(actorSystemName = x) } text("Name of the actor system")
    }
    parser.parse(args, Commandline()) match {
      case Some(clConfig) =>
        val config = appConfig.withFallback(ConfigFactory.load())
        val rolesFromConfig = config.getString("styx.cluster.roles").split(",")
        val roles:Seq[String] =
          if(clConfig.roles.length != 0)
            clConfig.roles
          else if(rolesFromConfig.length !=0 && rolesFromConfig(0).size != 0)
            rolesFromConfig
          else
            defaultRoles
        val seedsFromConfig = config.getString("styx.cluster.seed_nodes").split(",")
        val seeds:Seq[String] = if(clConfig.seeds.length != 0) clConfig.seeds else seedsFromConfig
        val actorSystemName = if(!clConfig.actorSystemName.isEmpty()) clConfig.actorSystemName else config.getString("styx.actor-system-name")
        val port = if(clConfig.port != Int.MaxValue) clConfig.port else config.getInt("styx.cluster.port")
        clConfig.copy(roles = roles, seeds=seeds, actorSystemName = actorSystemName, port=port)
      case None =>
        null
    }
  }
}
