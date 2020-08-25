package com.project4;

import java.util.Scanner;

/**
 * Abstract class for sharing code between Main methods for client and server.
 */
public abstract class Main {

  protected static Version getVersion(Scanner scanner) {
    System.out.print("Specify TCP [T], UDP [U], or RPC [R]: ");
    String protocol = scanner.nextLine();
    Version version;
    switch (protocol) {
      case "T":
      case "TCP":
      case "t":
      case "tcp":
        version = Version.TCP;
        break;
      case "U":
      case "UDP":
      case "u":
      case "udp":
        version = Version.UDP;
        break;
      case "R":
      case "RPC":
      case "r":
      case "rpc":
        version = Version.RPC;
        break;
      default:
        System.err.println("For protocol, specify TCP [T], UDP [U], or RPC [R]");
        version = Version.ERROR;
    }
    return version;
  }

  protected enum Version {
    TCP,
    UDP,
    RPC,
    ERROR;
  }
}
