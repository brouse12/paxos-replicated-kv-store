package com.project4;

import com.project4.client.Client;
import com.project4.client.RPCClient;
import com.project4.client.TCPClient;
import com.project4.client.UDPClient;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * Main function to communicate with a server application's key-value store.  Supports TCP, UDP, and
 * RPC via Java RMI, and operates in either Manual Mode or Script Mode.  See README.  Available
 * operations are GET, PUT, DELETE, and EXIT.  See Executive Summary regarding protocol used.
 */
public class ClientMain extends Main {
  private static final int INSTRUCTION_INDEX = 0;
  private static final int KEY_INDEX = 1;
  private static final int VALUE_INDEX = 2;

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: java -jar client [host] [port]");
      System.exit(1);
    }

    Scanner scanner = new Scanner(System.in);
    Version version = getVersion(scanner);
    if (version == null || version == Version.ERROR) {
      System.err.println("For protocol, specify TCP [T] or UDP [U] or RPC [R]");
      System.exit(2);
    }

    System.out.print("Specify Script Mode [S] or Manual Mode [M]: ");
    String mode = scanner.nextLine();
    boolean scriptMode = true;
    switch (mode) {
      case "S":
      case "s":
      case "Script Mode":
      case "script mode":
        break;
      case "M":
      case "m":
      case "Manual Mode":
      case "manual Mode":
        scriptMode = false;
        break;
      default:
        System.err.println("For mode, specify Script Mode [S] or Manual Mode [M]");
        scanner.close();
        System.exit(3);
    }

    String host = args[0];
    String port = args[1];
    Client client = null;
    try {
      switch (version) {
        case TCP:
          client = new TCPClient(host, port);
          break;
        case UDP:
          client = new UDPClient(host, port);
          break;
        case RPC:
          client = new RPCClient(host, port);
      }
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      System.exit(4);
    }

    if (scriptMode) {
      System.out.print("Enter name of script file: ");
      String script = scanner.nextLine();
      scanner.close();
      preloadValues(client);
      runScriptMode(client, script);
    } else {
      preloadValues(client);
      runManualMode(client, scanner);
      scanner.close();
    }
  }

  private static void preloadValues(Client client) {
    boolean connected = client.openConnection();
    if (!connected) {
      System.err.println(currentTime() + ">> Could not connect to server. Exiting.");
      System.exit(5);
    }
    System.out.println("Preloading values to key value store:");
    try (BufferedReader script = new BufferedReader(new FileReader("script.txt"))) {
      String line;
      while ((line = script.readLine()) != null) {
        System.out.println(currentTime() + ">> Sending instruction: " + line);
        processCommand(client, line);
      }
    } catch (FileNotFoundException e) {
      System.err.println(currentTime() + ">> Could not identify file.");
      client.closeConnection();
      System.exit(5);
    } catch (IOException e) {
      System.err.println(currentTime() + ">> IOException while reading file.");
      client.closeConnection();
      System.exit(5);
    }

  }

  private static void runScriptMode(Client client, String scriptName) {
    try (BufferedReader script = new BufferedReader(new FileReader(scriptName))) {
      String line;
      while ((line = script.readLine()) != null) {
        System.out.println(currentTime() + ">> Sending instruction: " + line);
        processCommand(client, line);
      }
    } catch (FileNotFoundException e) {
      System.err.println(currentTime() + ">> Could not identify file.");
    } catch (IOException e) {
      System.err.println(currentTime() + ">> IOException while reading file.");
    }
    client.closeConnection();
    System.out.println(currentTime() + ">> Exiting.");
  }

  private static void runManualMode(Client client, Scanner scanner) {

    System.out.println("Entering manual mode. Valid commands:\n" +
            "put [key] [value]\n" +
            "get [key]\n" +
            "del [key]\n" +
            "change_server [host] [port]\n" +
            "exit\n");
    String input = "";
    while (!input.equals("exit")) {
      System.out.print(currentTime() + ">> Command: ");
      input = scanner.nextLine();
      processCommand(client, input);
    }
    client.closeConnection();
    System.out.println(currentTime() + ">> Exiting.");
  }

  private static void processCommand(Client client, String command) {
    String[] tokens = command.split(" ");
    switch (tokens[INSTRUCTION_INDEX].toLowerCase()) {
      case "put":
        if (tokens.length != 3) {
          System.out.println(currentTime() + ">> Invalid command.");
        } else {
          client.put(tokens[KEY_INDEX], tokens[VALUE_INDEX]);
        }
        break;
      case "get":
        if (tokens.length != 2) {
          System.out.println(currentTime() + ">> Invalid command.");
        } else {
          String outcome = client.get(tokens[KEY_INDEX]);
          if (outcome != null) {
            System.out.println(currentTime() + ">> Received: " + outcome);
          } else {
            System.out.println(currentTime() + ">> No value returned");
          }
        }
        break;
      case "del":
        if (tokens.length != 2) {
          System.out.println(currentTime() + ">> Invalid command.");
        } else {
          client.delete(tokens[KEY_INDEX]);
        }
        break;
      case "change_server":
        if (tokens.length != 3) {
          System.out.println(currentTime() + ">> Invalid command.");
        } else {
          boolean success = client.change_server(tokens[1], tokens[2]);
          if (!success) {
            System.err.println(currentTime() + ">> Error. Could not change to specified server.");
          }
        }
        break;
      case "exit":
        break;
      default:
        System.out.println(currentTime() + ">> Invalid command.");
    }
  }

  private static String currentTime() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    return formatter.format(System.currentTimeMillis());
  }
}
