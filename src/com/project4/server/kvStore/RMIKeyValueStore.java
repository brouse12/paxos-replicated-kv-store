package com.project4.server.kvStore;

import com.project4.server.kvStore.paxos.Acceptor;
import com.project4.server.kvStore.paxos.GrantedMessage;
import com.project4.server.kvStore.paxos.Learner;
import com.project4.server.kvStore.paxos.PaxosMessenger;
import com.project4.server.kvStore.paxos.PermissionOutcome;
import com.project4.server.kvStore.paxos.ProposalQueue;
import com.project4.server.kvStore.paxos.Proposer;
import com.project4.server.kvStore.paxos.SuggestionId;
import com.project4.server.kvStore.paxos.Value;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a Key Value store, to be stored and accessed by a server using the KeyValueStore
 * interface.  Fault-tolerant consensus amongst replicas is achieved through Paxos. Replicas
 * communicate via RMI using the PaxosMessenger interface.  The cluster is expected to consist of 5
 * servers, and so assumes that 3 servers constitute a majority for Paxos.
 */
public class RMIKeyValueStore extends UnicastRemoteObject implements PaxosMessenger, KeyValueStore, Learner {
  private static final String URL_NAME = "KVStore";
  private static final int DEFAULT_RMI_PORT = 1099;
  private static final String SERVER_CONFIG_FILE = "serverConfig.txt";
  private static final int SERV_ID = 0;
  private static final int HOST = 1;
  private static final int REPLICA_MAJORITY = 3;

  private String myId = null; // Unique id to identify this process for RMI purposes
  private Map<String, String> kvStore = new HashMap<>(); // The KV store
  private List<PaxosMessenger> replicas = new CopyOnWriteArrayList<>(); // Registered data replica servers
  private Map<String, String> replicaRegistrationData = new HashMap<>(); // Data to register replica servers
  private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

  // Classes for Paxos implementation
  private ProposalQueue queue = new ProposalQueue();
  private Acceptor acceptor = new Acceptor();

  // --------------------------------- Constructor methods ----------------------------------------

  public RMIKeyValueStore() throws RemoteException {
    super();
  }

  @Override
  public void run(String myID) throws RemoteException, MalformedURLException, IllegalArgumentException {
    this.myId = myID;
    Naming.rebind(URL_NAME + myId, this);
    readConfigFile();
    for (String id : replicaRegistrationData.keySet()) {
      addReplica(replicaRegistrationData.get(id), id);
    }
    replicas.add(this);
    System.out.println(currentTime() + ">> Replicas found: " + (replicas.size() - 1));
    Proposer proposer = new Proposer(queue, this, myId);
    proposer.start();
  }

  private void readConfigFile() {
    try (BufferedReader script = new BufferedReader(new FileReader(SERVER_CONFIG_FILE))) {
      String line;
      while ((line = script.readLine()) != null) {
        String[] remoteServData = parseConfigLine(line);
        if (!remoteServData[SERV_ID].equals(myId)) {
          replicaRegistrationData.put(remoteServData[SERV_ID], remoteServData[HOST]);
        }
      }
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("Could not locate server config file: " + SERVER_CONFIG_FILE);
    } catch (IOException e) {
      throw new IllegalArgumentException("IO exception while reading server config file: " + SERVER_CONFIG_FILE);
    }
  }

  private String[] parseConfigLine(String line) throws IllegalArgumentException {
    String[] parsedLine = line.split(" ");
    if (parsedLine.length != 2) {
      throw new IllegalArgumentException("Invalid config file entry: " + line);
    }
    return parsedLine;
  }

  private void addReplica(String host, String id) throws RemoteException {
    String registryURL = "rmi://" + host + ":" + DEFAULT_RMI_PORT
            + "/" + URL_NAME + id;

    PaxosMessenger replica = bindReplica(registryURL); // Connect to remote replica
    if (replica == null) {
      return;
    }
    boolean success;
    try {
      success = replica.registerThisId(myId); // Set up reverse connection
    } catch (ConnectException e) {
      return;
    }
    if (!success) {
      throw new RemoteException("Server with Id= " + id + " Host= "
              + host + " could not bind to this server.");
    }
    replicas.add(replica);
  }

  private PaxosMessenger bindReplica(String registryURL) {
    PaxosMessenger replica;
    try {
      replica = (PaxosMessenger) Naming.lookup(registryURL);
    } catch (RemoteException | MalformedURLException | NotBoundException e) {
      return null;
    }
    return replica;
  }

  @Override
  public boolean registerThisId(String id) {
    if (replicaRegistrationData.get(id) == null) {
      return false;
    }
    String registryURL = "rmi://" + replicaRegistrationData.get(id) + ":" + DEFAULT_RMI_PORT
            + "/" + URL_NAME + id;
    PaxosMessenger replica = bindReplica(registryURL);
    if (replica == null) {
      return false;
    }
    replicas.add(replica);
    return true;
  }

  // --------------------- Methods to update the KeyValueStore cluster ---------------------------


  @Override
  public boolean put(String key, String value) {
    Value putValue = new Value(Command.PUT, key, value);
    queue.addTask(putValue);
    while (putValue.notExecuted()) {
      // Wait
    }
    return true;
  }

  @Override
  public String get(String key) {
    return kvStore.get(key);
  }

  @Override
  public boolean delete(String key) {
    Value getValue = new Value(Command.DELETE, key, null);
    queue.addTask(getValue);
    while (getValue.notExecuted()) {
      // Wait
    }
    return true;
  }

  // ------------------------------ Paxos Message Handling ----------------------------------------

  @Override
  public PermissionOutcome sendPermissionMessages(SuggestionId suggestionId, int stateId) {
    int numGranted = 0;
    SuggestionId highestAcceptedId = new SuggestionId(0, "");
    Value highestAcceptedValue = null;
    GrantedMessage response;
    for (PaxosMessenger replica : replicas) {
      try {
        response = replica.requestPermission(suggestionId, stateId);
      } catch (RemoteException e) {
        continue; // This replica has crashed and will be excluded
      }
      if (response.getPermission() == GrantedMessage.Permission.GRANTED) {
        numGranted++;
        if (response.getLastAcceptedId().compareTo(highestAcceptedId) > 0) {
          highestAcceptedId = response.getLastAcceptedId();
          highestAcceptedValue = response.getLastAcceptedValue();
        }
      }
      if (response.getPermission() == GrantedMessage.Permission.STATE_NACK) {
        // TODO: In a fully consistent version, the proposer will need to catch up its data.
        try {
          int newStateId = replica.getStateId();
          queue.setStateId(newStateId);
        } catch (RemoteException e) {
          // Do nothing
        }
        return new PermissionOutcome(false);
      }
    }
    if (numGranted >= REPLICA_MAJORITY) {
      return new PermissionOutcome(true, highestAcceptedValue);
    }
    return new PermissionOutcome(false);
  }

  @Override
  public boolean sendSuggestionMessages(SuggestionId suggestionId, Value value, int stateId) {
    int numAccepted = 0;
    boolean accepted;
    for (PaxosMessenger replica : replicas) {
      try {
        accepted = replica.suggestValue(suggestionId, value, stateId);
      } catch (RemoteException e) {
        continue; // This replica has crashed and will be excluded
      }
      if (accepted) {
        numAccepted++;
      }
    }
    return numAccepted >= REPLICA_MAJORITY;
  }

  @Override
  public void sendConsensusMessages(Value value, int stateId) {
    for (PaxosMessenger replica : replicas) {
      try {
        replica.reportConsensus(value, stateId);
      } catch (RemoteException e) {
        // Do nothing. This replica has crashed and will miss the consensus announcement
      }
    }
  }

  @Override
  public int getStateId() {
    return queue.getStateId();
  }


  // ------------------------------ Paxos Acceptor Handling ----------------------------------------

  @Override
  public GrantedMessage requestPermission(SuggestionId suggestionId, int stateId) {
    return acceptor.requestPermission(suggestionId, stateId, queue);
  }

  @Override
  public boolean suggestValue(SuggestionId suggestionId, Value value, int stateId) {
    return acceptor.suggestValue(suggestionId, value, stateId, queue);
  }

  // ---------------------------- Paxos Learner Handling ------------------------------------------

  @Override
  public void reportConsensus(Value value, int stateId) {
    if (stateId >= queue.getStateId()) {
      queue.setStateId(stateId + 1);
    }
    queue.dequeue(value);
    executeValue(value);
  }

  private synchronized void executeValue(Value value) {
    if (value.getCommand() == Command.PUT) {
      kvStore.put(value.getKey(), value.getValue());
    } else if (value.getCommand() == Command.DELETE) {
      kvStore.remove(value.getKey());
    }
  }
  // ------------------------------- Helper functions and classes ----------------------------------

  private String currentTime() {
    return dateFormatter.format(System.currentTimeMillis());
  }
}
