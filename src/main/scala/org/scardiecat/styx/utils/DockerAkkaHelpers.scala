package org.scardiecat.styx

import com.typesafe.config.{ConfigValueFactory, ConfigFactory, Config}
import org.scardiecat.styx.utils.DockerIpAndPortProvider
import org.scardiecat.styx.utils.commandline.Commandline

import scala.collection.JavaConversions

object DockerAkkaUtils {
  def dockerAkkaConfigProvider(config: Config,  localAdress: String, commandline: Commandline): Config = {
    val ipAndPort: DockerIpAndPortProvider = DockerIpAndPortProvider(localAdress, commandline.port)
    val newConfig:Config = config
      .withValue("akka.cluster.roles", ConfigValueFactory.fromIterable(
        JavaConversions.asJavaIterable(commandline.roles.map(role => s"$role").toIterable)))
      .withValue("akka.cluster.seed-nodes", ConfigValueFactory.fromIterable(
        JavaConversions.asJavaIterable(commandline.seeds.map(seedLoc => s"akka.tcp://${commandline.actorSystemName}@$seedLoc").toIterable)))
      .withValue("akka.remote.netty.tcp.bind-hostname", ConfigValueFactory.fromAnyRef(localAdress))
      .withValue("akka.remote.netty.tcp.bind-port", ConfigValueFactory.fromAnyRef(commandline.port))
      .withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(ipAndPort.hostIP))
      .withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(ipAndPort.akkaPort))
      .withFallback(ConfigFactory.load())

    System.out.println("Commandline: "+commandline)
    System.out.println("Binding core internally on " + newConfig.getString("akka.remote.netty.tcp.bind-hostname") + " port " + newConfig.getString("akka.remote.netty.tcp.bind-port"))
    System.out.println("Binding core logically on " + newConfig.getString("akka.remote.netty.tcp.hostname") + " port " + newConfig.getString("akka.remote.netty.tcp.port"))
    System.out.println("Seeds: " + newConfig.getList("akka.cluster.seed-nodes").toString)
    System.out.println("Roles: " + newConfig.getList("akka.cluster.roles").toString)
    newConfig
  }
}
