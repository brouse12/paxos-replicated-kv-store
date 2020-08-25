package com.project4;

import com.project4.server.RMIServer;
import com.project4.server.RPCServer;
import com.project4.server.Server;
import com.project4.server.TCPServer;
import com.project4.server.UDPServer;

import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * Main function for a server acting as a key-value store.  Supports TCP UDP, and RPC via Java RMI.
 * See README. Available operations are GET, PUT, and DELETE. The server is single-threaded (except
 * in RPC mode) but can queue multiple clients. However, multiple replicas of the server can be
 * maintained to increase availability.  The replicas communicate via RMI. Currently, there is no
 * mechanism to shut down the server while it is running. See Executive Summary regarding protocol
 * used.  In RPC mode, the server does not bind to the port, but uses the value to create a unique
 * RMI URL.
 */
public class ServerMain extends Main {

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: java -jar server [port] [server_id]");
      System.exit(1);
    }

    Scanner scanner = new Scanner(System.in);
    Version version = getVersion(scanner);
    scanner.close();
    if (version == null || version == Version.ERROR) {
      System.err.println("For protocol, specify TCP [T], UDP [U], or RPC [R]");
      System.exit(2);
    }

    String port = args[0];
    if (version == Version.RPC) {
      try {
        RPCServer server = new RMIServer();
        server.run(port, args[1]);
      } catch (RemoteException e) {
        System.err.println("RemoteError while setting up RPC server.");
        e.printStackTrace();
        System.exit(3);
      } catch (IllegalArgumentException e) {
        System.err.println(e.getMessage());
        System.exit(4);
      }
    } else {
      Server server = null;
      try {
        server = (version == Version.TCP) ? new TCPServer(port, args[1]) : new UDPServer(port, args[1]);
      } catch (IllegalArgumentException e) {
        System.err.println(e.getMessage());
        System.exit(4);
      } catch (RemoteException e) {
        System.err.println("Remote Exception. Is the RMI registry running?\nMessage: " + e.getMessage());
        e.printStackTrace();
        System.exit(4);
      }
      server.run();
    }
  }
}

