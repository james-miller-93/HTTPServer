//ThreadPoolServer2.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

public class ThreadPoolServer2 {

  static int port, cacheSize, threadSize;

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
    threadSize = ch.getThreadSize();
    String monString = ch.getMonitorString();
    
    //HashMap<String,String> docRootServerMap = configHandler(config_file);
    
    Socket connectionSocket = new Socket();
    ArrayList<Socket> connections = new ArrayList<Socket>();

    ServerSocket listenSocket = new ServerSocket(port);

    Thread threadPool[] = new Thread[threadSize];

    ThreadPoolHandler2 tph2 = new ThreadPoolHandler2(connections, docRootServerMap, "ThreadPoolServer2", cache, monString);
    
    for (int i=0; i < threadSize; i++) {
      threadPool[i] = new Thread(tph2);
      threadPool[i].start();
    }

    while(true) {
      try {
        connectionSocket = listenSocket.accept();
        synchronized(connections) {
        connections.add(connectionSocket);
        }
      } catch (Exception e) {
        System.out.println("Server error");
      }
    }

  }

  static class ThreadPoolHandler2 implements Runnable {

    //WebRequestHandler wrh;
    ArrayList<Socket> connections;
    HashMap<String,String> serverMap;
    String serverType;
    Cache sharedCache;
    String monString;

    public ThreadPoolHandler2(ArrayList<Socket> connections, HashMap<String,String> serverMap, String serverType, Cache sharedCache, String monString) throws Exception {
      //WebRequestHandler webHandler = new WebRequestHandler(connectionSocket, serverMap, serverType, sharedCache);
      //this.wrh = WebHandler;
      this.connections = connections;
      this.serverMap = serverMap;
      this.serverType = serverType;
      this.sharedCache = sharedCache;
      this.monString = monString;
    }

    public void run() {

      while (true) {

          Socket connectionSocket = null;
          
          while(connectionSocket == null) {
            synchronized(connections) {
            
              if (!connections.isEmpty()) {
               connectionSocket = (Socket) connections.remove(0);
              }
            }
          }
        try {
          WebRequestHandler wrh = new WebRequestHandler(connectionSocket, this.serverMap, this.serverType, this.sharedCache, this.monString);
          wrh.processRequest();

          connectionSocket.close();
            
          
        } catch(Exception e) {

        }
      }
    }

  }



}
