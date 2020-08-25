package com.project4.server.kvStore.paxos;

import com.project4.server.kvStore.Command;

import java.io.Serializable;

/**
 * A "Value" to be used for Paxos proposals.  Each value is a requested client command, either PUT
 * or DELETE.  Values track whether or not they have been executed by the cluster.
 */
public class Value implements Serializable {
  private Command command;
  private String key;
  private String value;
  private boolean executed = false;

  public Value(Command command, String key, String value) {
    this.command = command;
    this.key = key;
    this.value = value;
  }

  public Value() {
    this.command = Command.NO_OP;
    this.key = null;
    this.value = null;
  }

  public Command getCommand() {
    return command;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public synchronized void setExecuted() {
    executed = true;
  }

  public synchronized boolean notExecuted() {
    return !executed;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Value) {
      Value otherVal = (Value) obj;
      boolean keyMatch = this.key != null && this.key.equals(otherVal.key);
      boolean valMatch = this.value != null && this.value.equals(otherVal.value);
      return keyMatch && valMatch && this.command == otherVal.command;
    }
    return false;
  }
}
