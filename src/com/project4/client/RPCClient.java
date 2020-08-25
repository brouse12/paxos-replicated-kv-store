package com.project4.client;

import com.project4.server.RPCServer;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * RPC client for communicating with a server application's key-value store. See corresponding
 * interface and abstract class.
 */
public class RPCClient extends AbstractClient implements Client {
  private static final int RMI_DEFAULT_PORT = 1099;
  private static final String URL_NAME = "kvinterface";
  private RPCServer store = null;

  public RPCClient(String host, String port) {
    super(host, port);
  }

  public RPCClient(String host, int port) {
    super(host, port);
  }

  @Override
  public boolean openConnection() {
    String hostname = host.getHostName();
    try {
      if (hostname.equals(InetAddress.getLocalHost().getHostAddress())) {
        hostname = "localhost";
      }
    } catch (UnknownHostException e) {
      // Do nothing
    }
    String registryURL = "rmi://" + hostname + ":" + RMI_DEFAULT_PORT + "/" + URL_NAME + port;
    try {
      store = (RPCServer) Naming.lookup(registryURL);
    } catch (RemoteException | MalformedURLException | NotBoundException e) {
      return false;
    }
    return true;
  }

  @Override
  public void closeConnection() {
    // Do nothing. For TCP and UDP only.
  }

  @Override
  public void put(String key, String value) {
    try {
      store.put(key, value);
    } catch (RemoteException e) {
      System.err.println(currentTime() + ">> RemoteException while accessing server");
    }
  }

  @Override
  public String get(String key) {
    String result = null;
    try {
      result = store.get(key);
    } catch (RemoteException e) {
      System.err.println(currentTime() + ">> RemoteException while accessing server");
    }
    return result;
  }

  @Override
  public void delete(String key) {
    try {
      store.delete(key);
    } catch (RemoteException e) {
      System.err.println(currentTime() + ">> RemoteException while accessing server");
    }
  }
}