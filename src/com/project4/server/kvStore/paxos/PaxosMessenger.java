package com.project4.server.kvStore.paxos;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for classes that handle Paxos message passing via Java RMI.  "Dispatch" methods are
 * called from classes with access to the RMI stubs, to initiate a message broadcast.  They do not
 * necessarily need to be called via RMI.  "Response" methods are called by the Dispatch methods to
 * get responses from remote replicas.
 */
public interface PaxosMessenger extends Remote {
  /**
   * Use to set up the server cluster.  Tells an existing server to register your server id as an
   * RMI stub for future communication.
   *
   * @param id id of this server
   * @return true if registration successful, otherwise false.
   * @throws RemoteException
   */
  boolean registerThisId(String id) throws RemoteException;

  // ----------------------------------- Dispatch Methods -----------------------------------------

  PermissionOutcome sendPermissionMessages(SuggestionId suggestionId, int stateId) throws RemoteException;

  boolean sendSuggestionMessages(SuggestionId suggestionId, Value value, int stateId) throws RemoteException;

  void sendConsensusMessages(Value value, int stateId) throws RemoteException;

  // ----------------------------------- Response Methods -----------------------------------------

  GrantedMessage requestPermission(SuggestionId suggestionId, int stateId) throws RemoteException;

  boolean suggestValue(SuggestionId suggestionId, Value value, int stateId) throws RemoteException;

  void reportConsensus(Value value, int stateId) throws RemoteException;

  int getStateId() throws RemoteException;
}
