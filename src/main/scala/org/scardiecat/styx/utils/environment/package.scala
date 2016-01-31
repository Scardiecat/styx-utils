package org.scardiecat.styx.utils

package object environment {
  val ENV_HOST_IP        = "HOST_IP"
  val ENV_HOST_PORT      = "HOST_PORT"
  val ENV_HOSTNAME       = "HOSTNAME"  // this is the docker autocreated hostname env variable
  val ENV_AWS_EXT_AKKA       = "AWS_EXT_AKKA" // if set use the aws external ip to connect to akka
  val ENV_ACTORSYTEM_NAME       = "AS_NAME" // name of the Actor System

  //Cluster
  val ENV_CLUSTER_LOG_INFO = "CLUSTER_LOG_INFO" // on||off
  val ENV_SEED_NODES = "CLUSTER_SEED_NODES"   // addresses if seed nodes in the form "localhost:2551,localhost:2552"
  val ENV_ROLES = "CLUSTER_ROLES"   // Roles of the node in the form "Role1,Role2"
}
