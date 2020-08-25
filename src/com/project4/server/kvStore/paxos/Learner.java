package com.project4.server.kvStore.paxos;

/**
 * Interface for a class that must handle Learner responsibilities.  Implementing classes are not
 * fulfilling consensus detection, in the sense that Paxos Learners typically do, but simply
 * responding to a consensus update.  See the Proposer class to understand how Learning is performed
 * in this implementation.
 */
public interface Learner {

  void reportConsensus(Value value, int stateId);
}
