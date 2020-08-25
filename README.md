# Key-Value Store with Paxos

A replicated KV store program with consensus achieved via the Paxos algorithm.  Implemented with Java.  Up to 5 replicated data servers and any number of clients can run on different processes.  Server nodes may exit or crash then rejoin the cluster at any time, as long as a majority of nodes remain active (3 or more out of 5).

Any number of client processes may concurrently update the cluster by sending GET, PUT, or DELETE requests.  Clients can choose to communicate via TCP, UDP, or RPC (using Java RMI) with any of the active servers.

As this is a proof-of-concept program, there are some limitations:
1. The servers do not implement a plan for solving livelock, which can occur if two proposers perpetually try to outbid each other with increasing suggestion ID's.  One way to solve this is to have the servers elect a distinguished proposer.
2. The servers do not implement a data catch-up plan for crashed nodes re-entering the cluster.  Upon re-entering the cluster, they will achieve consensus with other nodes as to what operations will be implemented henceforth, but will not check if they missed prior operations.  This could be solved by having servers check a replicated log of operations after joining the cluster.
3. The process for setting up inter-server communication is clunky.  The servers send Paxos-related messages via Java RMI, which requires every server to look up the name for every other server using a config file.  A group communication tool, such as JGroups, could be a better choice.
4. A cluster size of 5 is currently assumed, instead of being a dynamic value.

# Execution

1. Edit serverConfig.txt to indicate the ID and host for each 
server replica you wish to run (the file is preset to run on 
localhost only).  Format: [Id] [host]. At runtime, you may choose
which servers are actually run, identified by their ID in the config
file.

2. Go to the classfiles directory and start the rmi registry:  
    ```
    rmiregistry
    ```
    
    3. Start the servers.  In the starting directory (with a unique process and ID for each server, unique port if running on localhost):  
    ```
    java -jar server.jar <port> <Id>
    ```
   
   4. Run any number of clients, each on a unique process. One or 
more clients may connect to each server: 
    ```
    java -jar client.jar <host> <port>
    ```

You will be prompted to choose either TCP,
UDP, or RPC.  For the client, if you choose Script Mode, 
you will be asked to name a text file in the same directory.
See script.txt as a sample.  In all 3 modes, the client
will start by running script.txt to preload some values.

When in remote procedure call (RPC) mode, the server will override 
the submitted port value with 1099, the standard port for Java RMI, 
but will use the submitted port to find the server's RMI URL.
Classfiles are included to run Java RMI with the existing jar files.
Server ID is not needed to complete a client-server connection, but 
allows servers to communicate with each other in the background.  UDP 
mode automatically binds the client to port 6000.

The RPC mode server is multithreaded, and the TCP and UDP
servers are single threaded.  In any mode, multiple clients 
may access distinct servers simultaneously.
