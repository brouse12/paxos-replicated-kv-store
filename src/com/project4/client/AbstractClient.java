package com.project4.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

/**
 * Abstract class for sharing methods between different Client implementations.
 */
public abstract class AbstractClient implements Client {
  private static final int PORT_MIN = 1024;
  private static final int PORT_MAX = 65535;
  private static final int MAX_INPUT_LENGTH = 25;
  private static final String WHITESPACE_REGEX = ".*\\s+.*";
  protected static final int SERVER_TIMEOUT_MILLIS = 2000;

  protected InetAddress host;
  protected int port;
  private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");


  public AbstractClient(String host, int port) {
    if (host == null) {
      throw new IllegalArgumentException("Host cannot be null.");
    }
    try {
      this.host = InetAddress.getByName(host);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Received invalid host: " + host + ".");
    }
    this.port = port;
    validatePort();
  }

  public AbstractClient(String host, String port) {
    if (host == null) {
      throw new IllegalArgumentException("Host cannot be null.");
    }
    if (port == null) {
      throw new IllegalArgumentException("Port cannot be null.");
    }
    try {
      this.host = InetAddress.getByName(host);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Received invalid host: " + host + ".");
    }
    try {
      this.port = Integer.parseInt(port);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
              "Received port = " + port + ". Port must be a valid integer.");
    }
    validatePort();
  }

  protected void validatePort() throws IllegalArgumentException {
    if (this.port < PORT_MIN || this.port > PORT_MAX) {
      throw new IllegalArgumentException("Received port = " + port +
              ". Valid port range is between " + PORT_MIN + " and " + PORT_MAX + ".");
    }
  }

  // Confirms if a single argument token in a GET/PUT/DELETE command follows protocol rules
  protected boolean isInvalid(String input) {
    if (input == null) {
      return true;
    }
    if (input.length() > MAX_INPUT_LENGTH) {
      return true;
    }
    return input.matches(WHITESPACE_REGEX);
  }

  protected void printArgError() {
    System.out.println(currentTime() + ">> Keys/Values must contain no whitespace and be " +
            MAX_INPUT_LENGTH + " characters or less.");
  }

  protected String currentTime() {
    return dateFormatter.format(System.currentTimeMillis());
  }

  @Override
  public boolean change_server(String host, String port) {
    try {
      this.host = InetAddress.getByName(host);
    } catch (UnknownHostException e) {
      return false;
    }
    try {
      this.port = Integer.parseInt(port);
    } catch (NumberFormatException e) {
      return false;
    }
    closeConnection();
    boolean success = openConnection();
    if (!success) {
      return false;
    }
    return true;
  }
}


