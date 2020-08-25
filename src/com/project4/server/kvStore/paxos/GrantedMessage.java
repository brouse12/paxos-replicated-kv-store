package com.project4.server.kvStore.paxos;

import java.io.Serializable;

/**
 * Represents an Acceptor's response to a Proposer's request for permission to suggest a value.
 * Response types include: Granted, NACK (if the Acceptor is ignoring the request based on its
 * suggestion id), and STATE NACK (if the Acceptor is ignoring the request based on its state id).
 */
public class GrantedMessage implements Serializable {
  private Permission permission;
  private SuggestionId lastAcceptedId;
  private Value lastAcceptedValue;

  public GrantedMessage(Permission permission, SuggestionId lastAcceptedId, Value lastAcceptedValue) {
    this.permission = permission;
    this.lastAcceptedId = lastAcceptedId;
    this.lastAcceptedValue = lastAcceptedValue;
  }

  public Permission getPermission() {
    return permission;
  }

  public SuggestionId getLastAcceptedId() {
    return lastAcceptedId;
  }

  public Value getLastAcceptedValue() {
    return lastAcceptedValue;
  }

  public enum Permission {
    GRANTED,
    NACK,
    STATE_NACK,
  }
}
