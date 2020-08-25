package com.project4.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote object representing a Key Value store interface, to be accessed by a client via RMI.
 */
public interface RPCServer extends Remote {

  /**
   * Runs the server indefinitely.
   */
  void run(String port, String servID) throws RemoteException;

  /**
   * Requests a put operation on the key-value store server.
   *
   * @param key   the key
   * @param value the value
   */
  void put(String key, String value) throws RemoteException;

  /**
   * Requests a get operation on the key-value store server.
   *
   * @param key the key
   * @return the value associated with this key, or '[NULL]' if none
   */
  String get(String key) throws RemoteException;

  /**
   * Requests a get operation on the key-value store server.
   *
   * @param key the key
   */
  void delete(String key) throws RemoteException;
}
