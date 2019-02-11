//PerTheadServer.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;

public class PerThreadServer {

  static int port, cacheSize;

  public static void main(String args[]) throws Exception {

        // Get command line argument.
    if (args.length != 2 || !args[0].equals("-config")) {
    
      System.out.println("Usage: -config <config_file_name>");
      return;
    }
        
        
    String config_file = args[1];

    ConfigurationHandler ch = new ConfigurationHandler(config_file);
    HashMap<String,String> docRootServerMap = ch.getConfigMap();
    port = ch.getPort();
    cacheSize = ch.getCacheSize();
    Cache cache = new Cache(cacheSize);

    String monString = ch.getMonitorString();
    //HashMap<String,String> docRootServerMap = configHandler(config_file);
    
    Socket connectionSocket = new Socket();

    ServerSocket listenSocket = new ServerSocket(port);

    while (true) {

      try {

        connectionSocket = listenSocket.accept();
        //BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        //String clientLine = inFromClient.readLine();
        
        ThreadWebRequestHandler twrh = new ThreadWebRequestHandler(connectionSocket, docRootServerMap, "PerThreadServer", cache, monString);
        
        Thread t = new Thread(twrh);
        t.start();

      
      } catch (Exception e) {

        connectionSocket.close();
      
      }      

    }

  }


}
