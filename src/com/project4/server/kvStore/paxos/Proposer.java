package com.project4.server.kvStore.paxos;

import java.rmi.RemoteException;
import java.util.NoSuchElementException;

/**
 * A Proposer, for Paxos purposes.  This class operates on a separate thread and acts as a consumer
 * of values added to the proposal queue.  When the queue has values, the Proposer will attempt to
 * get permission to suggest a value to the cluster, and will use values from the queue when Paxos
 * rules permit.
 * <p>
 * This class also acts as a Designated Learner.  After collecting Accepted messages from the
 * cluster, it will know if a consensus has been reached. If so, it will send updates to classes
 * that implement the Learner interface.
 */
public class Proposer extends Thread {
  ProposalQueue queue;
  private PaxosMessenger messenger;
  private String servId;
  private int suggestionId;


  public Proposer(ProposalQueue queue, PaxosMessenger messenger, String servId) {
    this.queue = queue;
    this.messenger = messenger;
    this.servId = servId;
    this.suggestionId = 0;
  }

  @Override
  public void run() {
    while (true) {
      while (queue.isEmpty()) {
        // Do nothing. We have no values to propose.
      }
      suggestionId++;
      int stateId = queue.getStateId();
      try {
        SuggestionId suggestNum = new SuggestionId(suggestionId, servId);
        PermissionOutcome outcome = messenger.sendPermissionMessages(suggestNum, stateId);
        if (!outcome.majority) {
          continue;
        }
        Value suggestedValue = chooseSuggestion(outcome);
        boolean suggestionAccepted = messenger.sendSuggestionMessages(suggestNum, suggestedValue, stateId);
        if (suggestionAccepted) {
          messenger.sendConsensusMessages(suggestedValue, stateId);
        }
      } catch (RemoteException e) {
        // Do nothing
      }
    }
  }

  // Choose the value provided during the permission request phase, or a value from the proposal
  // queue if nothing comes back from the cluster.
  private Value chooseSuggestion(PermissionOutcome outcome) {
    Value suggestedValue;
    if (outcome.highestAcceptedValue != null) {
      suggestedValue = outcome.highestAcceptedValue;
    } else {
      try {
        suggestedValue = queue.peek();
      } catch (NoSuchElementException e) {
        suggestedValue = new Value();
      }
    }
    return suggestedValue;
  }
}
