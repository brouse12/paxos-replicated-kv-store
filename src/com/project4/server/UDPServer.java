package com.project4.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;

/**
 * UDP server acting as a key-value store. See corresponding interface and abstract class.
 */
public class UDPServer extends AbstractServer {
  private static final int BUFFER_SIZE = 128;

  public UDPServer(String port, String servID) throws IllegalArgumentException, RemoteException {
    super(port, servID);
  }

  public UDPServer(int port, String servID) throws IllegalArgumentException, RemoteException {
    super(port, servID);
  }

  @Override
  public void run() {
    try (DatagramSocket server = new DatagramSocket(port)) {
      while (running) {
        System.out.println(currentTime() + ">> Waiting for client packets");
        byte[] buf = new byte[BUFFER_SIZE];
        DatagramPacket clientInput = new DatagramPacket(buf, buf.length);
        server.receive(clientInput);
        DatagramPacket response = serviceClientRequest(clientInput);
        server.send(response);
      }
    } catch (SocketException e) {
      System.err.println(currentTime() + ">> Socket Exception while binding to port " + port);
    } catch (IOException e) {
      System.err.println(currentTime() + ">> IOException receiving packet on " + port);
    }
    System.out.println(currentTime() + "Exiting.");
  }

  private DatagramPacket serviceClientRequest(DatagramPacket request) {
    String[] tokens = parseRequest(request);
    RequestResult result;
    if (tokens == null) {
      result = null;
    } else {
      try {
        result = handleIndividualRequest(tokens);
      } catch (RemoteException e) {
        result = new RequestResult(
                "Remote Exception thrown: " + e.getMessage(), "ERROR");
      }
    }
    InetAddress addr = request.getAddress();
    int port = request.getPort();
    byte[] message;
    if (result != null) {
      System.out.println(currentTime() + ">> client=" + addr + " port=" + port +
              ": " + result.result);
      message = result.returnMessage.getBytes();
    } else {
      System.err.println(currentTime() + ">> Invalid instruction. Dropping request.");
      message = "ERROR".getBytes();
    }
    return new DatagramPacket(message, message.length, addr, port);
  }

  private String[] parseRequest(DatagramPacket request) {
    String received = new String(request.getData()).trim();
    System.out.println(currentTime() + ">> client=" + request.getAddress() +
            " port=" + request.getPort() + ": Received '" + received + "'");

    String[] tokens = received.split(" ");
    boolean validLength = validateLength(tokens);
    if (!validLength) {
      return null;
    }
    return tokens;
  }

}
