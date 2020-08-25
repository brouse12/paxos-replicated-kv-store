package com.project4.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;

/**
 * TCP server acting as a key-value store. See corresponding interface and abstract class.
 */
public class TCPServer extends AbstractServer {

  public TCPServer(String port, String servID) throws IllegalArgumentException, RemoteException {
    super(port, servID);
  }

  public TCPServer(int port, String servID) throws IllegalArgumentException, RemoteException {
    super(port, servID);
  }


  @Override
  public void run() {
    try (ServerSocket server = new ServerSocket(port)) {
      while (running) {
        System.out.println(currentTime() + ">> Waiting for client connection");
        serviceClientRequests(server);
      }
    } catch (IOException e) {
      System.err.println(currentTime() + ">> I/O Exception while binding to port " + port);
    }
    System.out.println(currentTime() + ">> Exiting.");
    System.exit(0);
  }

  private void serviceClientRequests(ServerSocket server) {
    try (
            Socket connection = server.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
    ) {
      System.out.println(currentTime() + ">> client=" + connection.getInetAddress() +
              " port=" + port + ": Accepted connection");
      String received;
      while ((received = in.readLine()) != null) {
        System.out.println(currentTime() + ">> client=" + connection.getInetAddress() +
                " port=" + port + ": Received '" + received + "'");
        String[] tokens = received.split(" ");
        boolean validLength = validateLength(tokens);
        if (!validLength) {
          out.println("ERROR");
          continue;
        }
        RequestResult result = null;
        try {
          result = handleIndividualRequest(tokens);
        } catch (RemoteException e) {
          result = new RequestResult(
                  "Remote Exception thrown: " + e.getMessage(), "ERROR");
        }
        if (result != null) {
          System.out.println(currentTime() + ">> client=" +
                  connection.getInetAddress() + " port=" + port + ": " + result.result);
          out.println(result.returnMessage);
        } else {
          System.err.println(currentTime() + ">> Invalid instruction. Dropping request.");
          out.println("ERROR");
        }

      }
      System.out.println(currentTime() + ">> client=" + connection.getInetAddress() +
              " port=" + port + ": Client closed the session.");
    } catch (IOException e) {
      System.err.println(
              currentTime() + ">> IOException while servicing client. Dropping connection.");
    }
  }
}
