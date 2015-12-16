# styx-utils
utils for the styx serverstack


For AWS docker support

## DockerIpAndPortProvider:

Add the following to /etc/sysconfig/docker OPTIONS="-H 0.0.0.0:5555 -H unix:///var/run/docker.sock"

Now all kinds of very useful Docker-related information is available on port 5555 inside your container.
