package com.project4.client;

/**
 * Interface for a client that communicates with a server application's key-value store.
 */
public interface Client {

  /**
   * Opens a communication channel with the server.
   *
   * @return true if connection or setup was successful, else false.
   */
  boolean openConnection();

  /**
   * Closes any resources opened by openConnection().
   */
  void closeConnection();

  /**
   * Requests a put operation on the key-value store server.
   *
   * @param key   the key
   * @param value the value
   */
  void put(String key, String value);

  /**
   * Requests a get operation on the key-value store server.
   *
   * @param key the key
   * @return the value associated with this key, or '[NULL]' if none
   */
  String get(String key);

  /**
   * Requests a get operation on the key-value store server.
   *
   * @param key the key
   */
  void delete(String key);

  /**
   * Requests to change connection to a different server in the cluster.
   *
   * @param host host of the new server
   * @param port port of the new server
   * @return true if successful, otherwise false
   */
  boolean change_server(String host, String port);

}
