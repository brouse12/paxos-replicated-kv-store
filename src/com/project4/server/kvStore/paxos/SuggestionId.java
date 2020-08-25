package com.project4.server.kvStore.paxos;

import java.io.Serializable;

/**
 * A suggestion id for Paxos.  Suggestions ids are compared by their suggestion number, with a
 * server's unique server id being the tiebreaker.
 */
public class SuggestionId implements Serializable, Comparable<SuggestionId> {
  private int suggestNum;
  private String serverId;

  public SuggestionId(int suggestNum, String serverId) {
    this.suggestNum = suggestNum;
    this.serverId = serverId;
  }

  @Override
  public int compareTo(SuggestionId o) {
    if (suggestNum > o.suggestNum) {
      return 1;
    }
    if (suggestNum < o.suggestNum) {
      return -1;
    }
    return serverId.compareTo(o.serverId);
  }
}
