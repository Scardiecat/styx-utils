package org.scardiecat.styx.utils

import co.blocke.scalajack.ScalaJack

import scala.util.Try

package object DockerIpPortProvider {
  // magic aws urls to get the current ip address
  val AWS_LOCAL_HOST_IP  = "http://169.254.169.254/latest/meta-data/local-ipv4"
  val AWS_PUBLIC_HOST_IP = "http://169.254.169.254/latest/meta-data/public-ipv4"

  val INTERNAL_AKKA_PORT = 2551

  val ENV_HOST_IP        = "HOST_IP"
  val ENV_HOST_PORT      = "HOST_PORT"
  val ENV_HOSTNAME       = "HOSTNAME"  // this is the docker autocreated hostname env variable
  val ENV_AWS_EXT_AKKA       = "AWS_EXT_AKKA" // if set use the aws external ip to connect to akka
}

import DockerIpPortProvider._

case class DockerIpAndPortProvider(defaultHost:String, defaultPort:String) {
  val baseResolver = {
    val base = new EnvVariableResolver(){}
    if( base.hostIP.isEmpty || base.akkaPort.isEmpty )
      base + AwsEnvironmentResolver()
    else
      base
  }
  val hostIP = baseResolver.hostIP.getOrElse(defaultHost)
  val akkaPort = baseResolver.akkaPort.getOrElse(defaultPort.toInt)
}

trait EnvVariableResolver {
  val hostIP   : Option[String] = Option(System.getenv().get(ENV_HOST_IP))
  val akkaPort : Option[Int]    = Option(System.getenv().get(ENV_HOST_PORT)).map(_.toInt)
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
  private val sj = ScalaJack()

  def httpGetLite( uri:String ) = Try{scala.io.Source.fromURL(uri,"utf-8").getLines.fold("")( (a,b) => a + b )}.toOption

  override val hostIP   : Option[String] = {
    if( System.getenv().get(ENV_AWS_EXT_AKKA) == "true" )
      httpGetLite(AWS_PUBLIC_HOST_IP)  // Akka callable only outside AWS
    else
      httpGetLite(AWS_LOCAL_HOST_IP)   // Akka callable only inside AWS
  }
  override val akkaPort : Option[Int] = httpGetLite(AWS_LOCAL_HOST_IP).flatMap(local => inspectPort(local))

  def inspectPort(hIp:String) : Option[Int] = {
    val instId = System.getenv().get(ENV_HOSTNAME)
    httpGetLite(s"http://$hIp:5555/containers/json").flatMap( ins => {
      Try {
        val ob = sj.read[List[DockerInspect]](ins)
        ob.find( _.Id.startsWith(instId) ).map(_.Ports.find( _.PrivatePort == INTERNAL_AKKA_PORT ).map( _.PublicPort.get ).get ).get
      }.toOption
    })
  }
}

// for docker containers json parsing
case class DockerInspect ( Id: String,Ports : List[PortBinding])

case class PortBinding(PrivatePort : Int,PublicPort  : Option[Int])
