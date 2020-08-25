package com.project4.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.function.Predicate;

/**
 * UDP client for communicating with a server application's key-value store. See corresponding
 * interface and abstract class.
 */
public class UDPClient extends AbstractClient {
  private DatagramSocket connection = null;
  private static final int LOCAL_PORT = 6000;
  private static final int BUFFER_SIZE = 128;

  public UDPClient(String host, String port) {
    super(host, port);
  }

  public UDPClient(String host, int port) {
    super(host, port);
  }


  @Override
  public boolean openConnection() {
    closeConnection();
    try {
      connection = new DatagramSocket(LOCAL_PORT);
      connection.setSoTimeout(SERVER_TIMEOUT_MILLIS);
    } catch (SocketException e) {
      return false;
    }
    return true;
  }

  @Override
  public void closeConnection() {
    if (connection != null) {
      connection.close();
    }
  }

  public void put(String key, String value) {
    if (isInvalid(key) || isInvalid(value)) {
      printArgError();
      return;
    }
    byte[] message = ("PUT " + key + " " + value).getBytes();
    DatagramPacket out = new DatagramPacket(message, message.length, host, port);
    try {
      connection.send(out);
    } catch (IOException e) {
      System.err.println(currentTime() + ">> IOException while sending to server.");
      return;
    }
    Predicate<String> validate = new ResponseValidator.ValidatePutResponse(key, value);
    getResponseAndValidate(validate);
  }

  public String get(String key) {
    if (isInvalid(key)) {
      printArgError();
      return null;
    }
    byte[] message = ("GET " + key).getBytes();
    DatagramPacket out = new DatagramPacket(message, message.length, host, port);
    try {
      connection.send(out);
    } catch (IOException e) {
      System.err.println(currentTime() + ">> IOException while sending to server.");
      return null;
    }
    Predicate<String> validate = new ResponseValidator.ValidateGetResponse(key);
    String response = getResponseAndValidate(validate);
    return response == null ? null : response.split(" ")[4];
  }

  public void delete(String key) {
    if (isInvalid(key)) {
      printArgError();
      return;
    }
    byte[] message = ("DEL " + key).getBytes();
    DatagramPacket out = new DatagramPacket(message, message.length, host, port);
    try {
      connection.send(out);
    } catch (IOException e) {
      System.err.println(currentTime() + ">> IOException while sending to server.");
      return;
    }
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
    String resp = null;
    boolean validMessage = false;
    while (!validMessage) {
      byte[] buf = new byte[BUFFER_SIZE];
      DatagramPacket serverResp = new DatagramPacket(buf, buf.length);
      try {
        connection.receive(serverResp);
      } catch (SocketTimeoutException e) {
        System.out.println(currentTime() + ">> No response. Server timed out.");
        return null;
      }
      resp = new String(serverResp.getData()).trim();
      validMessage = isValid.test(resp);
      if (!validMessage) {
        System.err.println(currentTime() + ">> Invalid response from server: " + resp);
      }
    }
    return resp;
  }
}
