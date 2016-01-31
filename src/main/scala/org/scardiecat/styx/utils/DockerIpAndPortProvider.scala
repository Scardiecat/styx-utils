package org.scardiecat.styx.utils

import org.scardiecat.styx.utils.docker.DockerEnvironmentInspector

import scala.util.Try

package object DockerIpPortProvider {
  // magic aws urls to get the current ip address
  val AWS_LOCAL_HOST_IP  = "http://169.254.169.254/latest/meta-data/local-ipv4"
  val AWS_PUBLIC_HOST_IP = "http://169.254.169.254/latest/meta-data/public-ipv4"

  val INTERNAL_AKKA_PORT = 2551
}

import DockerIpPortProvider._

case class DockerIpAndPortProvider(defaultHost:String, defaultPort:Int) {
  val baseResolver = {
    val base = new EnvVariableResolver(){}
    if( base.hostIP.isEmpty || base.akkaPort.isEmpty )
      base + AwsEnvironmentResolver()
    else
      base
  }
  val hostIP = baseResolver.hostIP.getOrElse(defaultHost)
  val akkaPort = baseResolver.akkaPort.getOrElse(defaultPort)
}

trait EnvVariableResolver {
  val hostIP   : Option[String] = Option(System.getenv().get(environment.ENV_HOST_IP))
  val akkaPort : Option[Int]    = Option(System.getenv().get(environment.ENV_HOST_PORT)).map(_.toInt)
  def +( ea:EnvVariableResolver ) : EnvVariableResolver  = {
    val left = this
    new EnvVariableResolver(){
      override val hostIP   : Option[String] = left.hostIP.orElse(ea.hostIP)
      override val akkaPort : Option[Int]    = left.akkaPort.orElse(ea.akkaPort)
    }
  }
}

// AWS EC2-specific Introspection
case class AwsEnvironmentResolver() extends EnvVariableResolver {
  private val parser = new DockerEnvironmentInspector()

  def httpGetLite( uri:String ) = {
    val hostname: Option[String] = Option(System.getenv().get(environment.ENV_HOSTNAME))
    if(hostname.isEmpty){
      // no hostname so it is really unlikely that we are on AWS. So skip
      None
    } else {
      Try {
        scala.io.Source.fromURL(uri, "utf-8").getLines.fold("")((a, b) => a + b)
      }.toOption
    }
  }

  override val hostIP   : Option[String] = {
    val hostname: Option[String] = Option(System.getenv().get(environment.ENV_HOSTNAME))
    if(hostname.isEmpty){
       // no hostname so it is really unlikely that we are on AWS. So skip
       None
    } else {
      if (System.getenv().get(environment.ENV_AWS_EXT_AKKA) == "true")
        httpGetLite(AWS_PUBLIC_HOST_IP) // Akka callable only outside AWS
      else
        httpGetLite(AWS_LOCAL_HOST_IP) // Akka callable only inside AWS
    }
  }
  override val akkaPort : Option[Int] = httpGetLite(AWS_LOCAL_HOST_IP).flatMap(local => inspectPort(local))

  def inspectPort(hIp:String) : Option[Int] = {
    val instId = System.getenv().get(environment.ENV_HOSTNAME)
    httpGetLite(s"http://$hIp:5555/containers/json").flatMap( ins => {
      Try {
        val containers = parser.parse(ins);
        // just assign to val to do the type conversion from Integer
        val port:Int = parser.findPort(containers, instId ,INTERNAL_AKKA_PORT)
        port
      }.toOption
    })
  }
}
