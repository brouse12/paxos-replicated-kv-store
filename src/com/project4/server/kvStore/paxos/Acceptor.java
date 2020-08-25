package com.project4.server.kvStore.paxos;

/**
 * Represents a Paxos Acceptor.  Messages from Proposers should be handled by this class.  The
 * Acceptor also stores the last accepted state id to support multi-paxos - it will clear its
 * previous accepted value when it sees that a message is concerned with a more recent state id.
 */
public class Acceptor {
  private SuggestionId lastPermittedId;
  private SuggestionId lastAcceptedId;
  private Value lastAcceptedValue;
  private int lastAcceptedStateId;

  public Acceptor() {
    this.lastPermittedId = new SuggestionId(0, "");
    this.lastAcceptedId = new SuggestionId(0, "");
    this.lastAcceptedValue = null;
    this.lastAcceptedStateId = 0;
  }

  public synchronized SuggestionId getLastPermittedId() {
    return lastPermittedId;
  }

  public synchronized SuggestionId getLastAcceptedId() {
    return lastAcceptedId;
  }

  public synchronized Value getLastAcceptedValue() {
    return lastAcceptedValue;
  }

  public synchronized int getLastAcceptedStateId() {
    return lastAcceptedStateId;
  }

  public synchronized void setLastPermittedId(SuggestionId lastPermittedId) {
    this.lastPermittedId = lastPermittedId;
  }

  public synchronized void setLastAcceptedId(SuggestionId lastAcceptedId) {
    this.lastAcceptedId = lastAcceptedId;
  }

  public synchronized void setLastAcceptedValue(Value lastAcceptedValue) {
    this.lastAcceptedValue = lastAcceptedValue;
  }

  public synchronized void setLastAcceptedStateId(int lastAcceptedStateId) {
    this.lastAcceptedStateId = lastAcceptedStateId;
  }

  public GrantedMessage requestPermission(SuggestionId suggestionId, int senderStateId, ProposalQueue queue) {
    if (senderStateId < queue.getStateId()) {
      return new GrantedMessage(GrantedMessage.Permission.NACK, null, null);
    }
    if (senderStateId > queue.getStateId()) {
      // TODO: In a fully consistent version, the accepter will need to catch up its data
      queue.setStateId(senderStateId);
    }
    if (senderStateId > getLastAcceptedStateId()) {
      setLastAcceptedValue(null); // This clears old values from a previous Paxos iteration for multi-Paxos
    }
    if (suggestionId.compareTo(getLastPermittedId()) >= 0) {
      setLastPermittedId(suggestionId);
      return new GrantedMessage(GrantedMessage.Permission.GRANTED,
              getLastAcceptedId(), getLastAcceptedValue());
    }
    return new GrantedMessage(GrantedMessage.Permission.NACK, null, null);
  }

  public boolean suggestValue(SuggestionId suggestionId, Value value, int senderStateId, ProposalQueue queue) {
    if (senderStateId < queue.getStateId()) {
      return false;
    }
    if (suggestionId.compareTo(getLastPermittedId()) >= 0) {
      setLastAcceptedId(suggestionId);
      setLastAcceptedValue(value);
      setLastAcceptedStateId(senderStateId);
      return true;
    }
    return false;
  }
}
