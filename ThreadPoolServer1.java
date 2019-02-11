//ThreadPoolServer1.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;

public class ThreadPoolServer1 {

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
    
    ServerSocket listenSocket = new ServerSocket(port);

    Thread threadPool[] = new Thread[threadSize];

    ThreadPoolHandler1 tph1 = new ThreadPoolHandler1(listenSocket, docRootServerMap, "ThreadPoolServer1", cache, monString);
    for (int i=0; i < threadSize; i++) {
      threadPool[i] = new Thread(tph1);
      threadPool[i].start();
    }
    for (int j=0; j < threadSize; j++) {
      threadPool[j].join();
    }

  }

  static class ThreadPoolHandler1 implements Runnable {

    //WebRequestHandler wrh;
    ServerSocket listenSocket;
    HashMap<String,String> serverMap;
    String serverType;
    Cache sharedCache;
    String monString;

    public ThreadPoolHandler1(ServerSocket listenSocket, HashMap<String,String> serverMap, String serverType, Cache sharedCache, String monString) throws Exception {
      //WebRequestHandler webHandler = new WebRequestHandler(connectionSocket, serverMap, serverType, sharedCache);
      //this.wrh = WebHandler;
      this.listenSocket = listenSocket;
      this.serverMap = serverMap;
      this.serverType = serverType;
      this.sharedCache = sharedCache;
      this.monString = monString;
    }

    public void run() {

      while (true) {

        try {
          Socket connectionSocket = listenSocket.accept();

          WebRequestHandler wrh = new WebRequestHandler(connectionSocket, this.serverMap, this.serverType, this.sharedCache, this.monString);
          wrh.processRequest();

          connectionSocket.close();
        } catch(Exception e) {

        }
      }
    }

  }



}
