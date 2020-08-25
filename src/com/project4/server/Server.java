package com.project4.server;

/**
 * Interface for a server acting as a key-value store.
 */
public interface Server {

  /**
   * Runs the server indefinitely.
   */
  void run();

  /**
   * Stops the server loop.  Currently, no class makes use of this method.
   */
  void stop();
}
