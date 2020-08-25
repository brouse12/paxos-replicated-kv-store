package com.project4.server.kvStore.paxos;

import java.util.LinkedList;

/**
 * Producer queue to store client requests.  The Proposer acts as a queue consumer and will suggest
 * these requests for cluster consensus.  The queue also tracks the "state id" for operations in the
 * cluster.  The first unanimous operation in the cluster has state id 1, the second has id 2, etc.
 * This allows participants to discover when they have fallen behind on consensus reports.
 */
public class ProposalQueue {
  private LinkedList<Value> queue;
  private int stateId;

  public ProposalQueue() {
    this.queue = new LinkedList<>();
    this.stateId = 1;
  }

  public synchronized void addTask(Value t) {
    queue.addLast(t);
  }

  public synchronized int getStateId() {
    return stateId;
  }

  public synchronized void setStateId(int id) {
    stateId = id;
  }

  public synchronized Value peek() {
    if (queue.size() == 0) {
      return null;
    }
    return queue.getFirst();
  }

  public synchronized boolean isEmpty() {
    return queue.size() == 0;
  }

  // Used to drop a value from the queue if the cluster has consented to that value.
  public synchronized void dequeue(Value value) {
    if (queue.size() == 0) {
      return;
    }
    if (queue.getFirst().equals(value)) {
      queue.getFirst().setExecuted();
      queue.removeFirst();
    }
  }
}