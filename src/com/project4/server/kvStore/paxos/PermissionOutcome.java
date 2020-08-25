package com.project4.server.kvStore.paxos;

import java.io.Serializable;

/**
 * Data structure to track the outcome of a request for permission to suggest a value.  The outcome
 * includes whether or not a cluster majority responded, and the previously accepted value with the
 * highest suggestion id, if applicable.
 */
public class PermissionOutcome implements Serializable {
  public boolean majority;
  public Value highestAcceptedValue;

  public PermissionOutcome(boolean majority, Value highestAcceptedValue) {
    this.majority = majority;
    this.highestAcceptedValue = highestAcceptedValue;
  }

  public PermissionOutcome(boolean majority) {
    this.majority = majority;
    this.highestAcceptedValue = null;
  }
}
