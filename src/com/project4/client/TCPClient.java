package com.project4.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.function.Predicate;

/**
 * TCP client for communicating with a server application's key-value store. See corresponding
 * interface and abstract class.
 */
public class TCPClient extends AbstractClient {
  private Socket connection = null;
  private BufferedReader in = null;
  private PrintWriter out = null;

  public TCPClient(String host, String port) {
    super(host, port);
  }

  public TCPClient(String host, int port) {
    super(host, port);
  }

  public boolean openConnection() {
    try {
      return openConnectionWithErrors();
    } catch (IOException e) {
      return false;
    }
  }

  private boolean openConnectionWithErrors() throws IOException {
    closeConnection();
    try {
      InetSocketAddress endPoint = new InetSocketAddress(host, port);
      connection = new Socket();
      connection.connect(endPoint, 50000);
      connection.setSoTimeout(SERVER_TIMEOUT_MILLIS);
    } catch (ConnectException e) {
      return false;
    }
    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    out = new PrintWriter(connection.getOutputStream(), true);
    return true;
  }

  public void closeConnection() {
    try {
      if (connection != null) {
        connection.close();
      }
      if (out != null) {
        out.close();
      }
      if (in != null) {
        in.close();
      }
    } catch (IOException e) {
      System.err.println(currentTime() + ">> Error closing connection with server.");
    }
  }

  public void put(String key, String value) {
    if (isInvalid(key) || isInvalid(value)) {
      printArgError();
      return;
    }
    out.println("PUT " + key + " " + value);
    Predicate<String> validate = new ResponseValidator.ValidatePutResponse(key, value);
    getResponseAndValidate(validate);
  }

  public String get(String key) {
    if (isInvalid(key)) {
      printArgError();
      return null;
    }
    out.println("GET " + key);
    Predicate<String> validate = new ResponseValidator.ValidateGetResponse(key);
    String response = getResponseAndValidate(validate);
    return response == null ? null : response.split(" ")[4];
  }

  public void delete(String key) {
    if (isInvalid(key)) {
      printArgError();
      return;
    }
    out.println("DEL " + key);
    Predicate<String> validate = new ResponseValidator.ValidateDelResponse(key);
    getResponseAndValidate(validate);
  }

  private String getResponseAndValidate(Predicate<String> isValid) {
    try {
      return getResponseWithErrors(isValid);
    } catch (IOException e) {
      System.err.println(currentTime() + ">> IOException while receiving server's response.");
      return null;
    }
  }

  private String getResponseWithErrors(Predicate<String> isValid) throws IOException {
    String response;
    try {
      while (!isValid.test(response = in.readLine())) {
        System.err.println(currentTime() + ">> Invalid response from server: " + response);
      }
    } catch (SocketTimeoutException e) {
      System.out.println(currentTime() + ">> No response. Server timed out.");
      return null;
    }
    return response;
  }
}



