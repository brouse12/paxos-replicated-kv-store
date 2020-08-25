package com.project4.server;

import com.project4.server.kvStore.KeyValueStore;
import com.project4.server.kvStore.RMIKeyValueStore;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;

/**
 * Remote object representing a Key Value store interface, to be accessed by a client via RMI.
 */
public class RMIServer extends UnicastRemoteObject implements RPCServer {
  private static final String URL_NAME = "kvinterface";
  private static final int DEFAULT_RMI_PORT = 1099;
  private static final int PORT_MIN = 1024;
  protected static final int PORT_MAX = 65535;


  private String servID = null;
  int port = -1;
  KeyValueStore kvStore = null;
  private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");


  public RMIServer() throws RemoteException {
    super();
  }

  @Override
  public void run(String port, String servID) throws RemoteException {
    try {
      this.port = Integer.parseInt(port);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
              "Received port = " + port + ". Port must be a valid integer.");
    }
    validatePort();
    this.servID = servID;
    startKvStore();
    try {
      Naming.rebind(URL_NAME + port, this);
      System.out.println(currentTime() + ">> Waiting for client requests");
    } catch (RemoteException e) {
      System.err.println(currentTime() + ">> Remote exception while binding sorter object: " + e.getMessage());
    } catch (MalformedURLException e) {
      System.err.println(currentTime() + ">> URL exception while binding sorter object: " + e.getMessage());
    }
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

  @Override
  public void put(String key, String value) throws RemoteException {
    boolean success = false;
    while (!success) {
      success = kvStore.put(key, value);
    }
    System.out.println(currentTime() + ">> port=" + DEFAULT_RMI_PORT + ": Put key=" + key + " value=" + value);
  }

  @Override
  public String get(String key) throws RemoteException {
    String value = kvStore.get(key);
    System.out.println(currentTime() + ">> port=" + DEFAULT_RMI_PORT + ": Get key=" + key + " value=" + value);
    return value;
  }

  @Override
  public void delete(String key) throws RemoteException {
    boolean success = false;
    while (!success) {
      success = kvStore.delete(key);
    }
    System.out.println(currentTime() + ">> port=" + DEFAULT_RMI_PORT + ": Delete key=" + key);
  }

  private String currentTime() {
    return dateFormatter.format(System.currentTimeMillis());
  }
}
