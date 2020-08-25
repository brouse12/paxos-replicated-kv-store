package com.project4.server;

import com.project4.server.kvStore.KeyValueStore;
import com.project4.server.kvStore.RMIKeyValueStore;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;

/**
 * Abstract class for sharing methods between different Server implementations.
 */
public abstract class AbstractServer implements Server {
  protected static final int PORT_MIN = 1024;
  protected static final int PORT_MAX = 65535;
  protected static final int INSTRUCTION_INDEX = 0;
  protected static final int KEY_INDEX = 1;
  protected static final int VALUE_INDEX = 2;
  protected static final int MAX_INPUT_LENGTH = 25;

  protected int port;
  protected String servID;
  KeyValueStore kvStore;
  protected boolean running = true;
  protected SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

  public AbstractServer(int port, String servID) throws IllegalArgumentException, RemoteException {
    this.servID = servID;
    this.port = port;
    validatePort();
    startKvStore();
  }

  public AbstractServer(String port, String servID) throws IllegalArgumentException, RemoteException {
    this.servID = servID;
    if (port == null) {
      throw new IllegalArgumentException("Port cannot be null.");
    }
    try {
      this.port = Integer.parseInt(port);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
              "Received port = " + port + ". Port must be a valid integer.");
    }
    validatePort();
    startKvStore();
  }

  private void validatePort() throws IllegalArgumentException {
    if (this.port < PORT_MIN || this.port > PORT_MAX) {
      throw new IllegalArgumentException("Received port = " + port +
              ". Valid port range is between " + PORT_MIN + " and " + PORT_MAX + ".");
    }
  }

  private void startKvStore() throws RemoteException, IllegalArgumentException {
    kvStore = new RMIKeyValueStore();
    try {
      kvStore.run(servID);
    } catch (MalformedURLException e) {
      throw new RemoteException(e.getMessage());
    }
  }

  public void stop() {
    running = false;
  }

  // Validates the size of GET/PUT/DELETE argument tokens pursuant to protocol
  protected boolean validateLength(String[] tokens) {
    for (String token : tokens) {
      if (token.length() > MAX_INPUT_LENGTH) {
        System.out.println(currentTime() + ">> Token '" + token +
                "' exceeds max length. Dropping request.");
        return false;
      }
    }
    return true;
  }

  // Based on provided client request tokens, calls the appropriate handling method
  protected RequestResult handleIndividualRequest(String[] request) throws RemoteException {
    switch (request[INSTRUCTION_INDEX]) {
      case "PUT":
        return handlePutRequest(request);
      case "GET":
        return handleGetRequest(request);
      case "DEL":
        return handleDeleteRequest(request);
      default:
        return null;
    }
  }

  private RequestResult handlePutRequest(String[] request) throws RemoteException {
    if (request.length != 3) {
      return null;
    }
    boolean success = false;
    while (!success) {
      success = kvStore.put(request[KEY_INDEX], request[VALUE_INDEX]);
    }
    String result = "Put key=" + request[KEY_INDEX] + " value=" + request[VALUE_INDEX];
    String returnMessage = "PUT " + request[KEY_INDEX] + " " + request[VALUE_INDEX];
    return new RequestResult(result, returnMessage);
  }

  private RequestResult handleGetRequest(String[] request) throws RemoteException {
    if (request.length != 2) {
      return null;
    }
    String value = kvStore.get(request[KEY_INDEX]);
    if (value == null) {
      value = "[NULL]";
    }
    String result = "Returned value=" + value + " for key=" + request[KEY_INDEX];
    String returnMessage = "GET KEY: " + request[KEY_INDEX] + " VAL: " + value;
    return new RequestResult(result, returnMessage);
  }

  private RequestResult handleDeleteRequest(String[] request) throws RemoteException {
    if (request.length != 2) {
      return null;
    }
    boolean success = false;
    while (!success) {
      success = kvStore.delete(request[KEY_INDEX]);
    }
    String result = "Deleted key=" + request[KEY_INDEX];
    String returnMessage = "DEL " + request[KEY_INDEX];
    return new RequestResult(result, returnMessage);
  }

  protected String currentTime() {
    return dateFormatter.format(System.currentTimeMillis());
  }

  /**
   * Simple class for storing the results of processing a single client request. Stores a summary of
   * the result, and the message to be returned to the client.
   */
  protected static class RequestResult {
    String result;
    String returnMessage;

    protected RequestResult(String result, String returnMessage) {
      this.result = result;
      this.returnMessage = returnMessage;
    }
  }
}


