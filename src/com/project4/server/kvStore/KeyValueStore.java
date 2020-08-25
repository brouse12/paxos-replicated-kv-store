package com.project4.server.kvStore;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

/**
 * Represents a Key Value store, to be stored by a TCP, UDP, or RPC server.  A KeyValueStore should
 * be set up to allow multiple replicas on different processes, facilitating availability and fault
 * tolerance. See README.
 */
public interface KeyValueStore{

  /**
   * Runs the server indefinitely.  Provide an identifier to distinguish this server from other
   * replicas.  RemoteException anticipated for errors in connecting to other servers.
   */
  void run(String myID) throws RemoteException, MalformedURLException;

  /**
   * Requests a put operation on the key-value store.
   *
   * @param key   the key
   * @param value the value
   */
  boolean put(String key, String value);

  /**
   * Requests a get operation on the key-value store.
   *
   * @param key the key
   * @return the value associated with this key, or '[NULL]' if none
   */
  String get(String key);

  /**
   * Requests a get operation on the key-value store.
   *
   * @param key the key
   */
  boolean delete(String key);
}
