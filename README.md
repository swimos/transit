# Transit

## Prerequisites

* [Install JDK 9+](https://www.oracle.com/technetwork/java/javase/downloads/index.html).
  * Ensure that your `JAVA_HOME` environment variable is pointed to your Java installation location.
  * Ensure that your `PATH` includes `$JAVA_HOME`.

* [Install Node.js](https://nodejs.org/en/).
  * Confirm that [npm](https://www.npmjs.com/get-npm) was installed during the Node.js installation.

## Run

### Windows

Install the [Windows Subsystem for Linux](https://docs.microsoft.com/en-us/windows/wsl/install-win10).

Execute the command `./run.sh` from a console pointed to the application's home directory. This will start a Swim server, seeded with the application's logic, on port 9002.
   ```console
    user@machine:~$ ./run.sh
   ```

### \*nix

Execute the command `./run.sh` from a console pointed to the application's home directory. This will start a Swim server, seeded with the application's logic, on port 9002.
   ```console
    user@machine:~$ ./run.sh
   ```

## View the UI
Open the following URL on your browser: http://localhost:9002.

## Run as a Fabric with two nodes

Run two Swim instances on your local machine to distribute the applications
Web Agents between the two processes.

```sh
# Build the UI
server $ ./build.sh

# Start the first fabric node in one terminal window:
server $ ./gradlew run -Dswim.config.resource=server-a.recon

# Start the second fabric node in another terminal window:
server $ ./gradlew run -Dswim.config.resource=server-b.recon
```

When both processes are up and running, you can point your browser at either
http://localhost:9008 (Server A) or http://localhost:9009 (Server B).  You
will see a live view of all Web Agents, regardless of which server you point
your browser at.  Swim transparently demultiplexes links opened by external
clients, and routes them to the appropriate server in the fabric.

## Run as a Fabric with a multi-node cluster
Each node in a fabric requires a slightly different configuration. Statically defining the configuration file for each 
node in a multi-node cluster is quite tedious. Programmatically computing the configurations for each node is
achievable provided the setup satisfies the following requirements
- each node in an 'n' node cluster has a unique ordinal index from x through x+n-1.
- the internal DNS name for a node with index 'x' will be `{$prefix}-x.{$suffix}:{$port}`
- each node in the cluster is addressable by every other node via a fully qualified DNS name

(These requirements are very similar to a [k8s stateful-set setup](https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/))

The following system properties/environment variables must be defined in each node   

`CLUSTER_PREFIX` - The substring before the index in the DNS name 

`CLUSTER_SUFFIX` - The substring after the index in the DNS name  

`CLUSTER_START` - The start index of the cluster

`CLUSTER_END` - The end index of the cluster

`SWIM_DB_PATH` - The directory path for the swim db files

`HOSTNAME` - The hostname of the specific node. This is unique to each node unlike the previous properties which are the
same for all the nodes. This is automatically available in some environments.

As an example, let's use k8s as the deployment tool for running this app in a cluster. Since swimos applications are 
stateful they need to deployed using k8s stateful sets. 

To run a 6 node cluster on port 9002 assuming the following service name and stateful set name:
```
Service (ns/name): swimos/transit
StatefulSet (ns/name): default/transit    
```

the system properties/environment variables to use will be:
```
CLUSTER_PREFIX=transit- 
CLUSTER_SUFFIX=.transit.swimos.svc.cluster.local:9002 
CLUSTER_START=0 
CLUSTER_END=5 
SWIM_DB_PATH=/tmp/swim-transit # or the mount point of your storage volume
```
`HOSTNAME` is an environment variable that is automatically available for each node using k8s stateful sets and it gets 
evaluated to `transit-{$index}`.

The way to run the app will be to start the `TransitPlane` class in each of the nodes with the appropriate system 
properties/environment variables.