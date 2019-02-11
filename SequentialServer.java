//SequentialServer.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;

public class SequentialServer {

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
    System.out.println(docRootServerMap.keySet());
    //HashMap<String,String> docRootServerMap = configHandler(config_file);
    
    Socket connectionSocket = new Socket();

    ServerSocket listenSocket = new ServerSocket(port);
    

    while (true) {

      try {

        connectionSocket = listenSocket.accept();
        //BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        //String clientLine = inFromClient.readLine();
        
        WebRequestHandler wrh = new WebRequestHandler(connectionSocket, docRootServerMap, "SequentialServer", cache, monString);
        wrh.processRequest();

      
      } catch (Exception e) {

        connectionSocket.close();
      
      }      

    }

  }

  public static HashMap<String,String> configHandler(String configFile) throws Exception {
    
    HashMap<String,String> configMap = new HashMap<String,String>();
    String key, value;

    BufferedReader br = new BufferedReader(new FileReader(configFile));
    String line;

    while((line = br.readLine()) != null) {
      String[] words = line.split("\\s");
      if (words[0].equals("Listen")) {
        port = Integer.parseInt(words[1]);
      } else if (words[0].equals("CacheSize")) {
        cacheSize = Integer.parseInt(words[1]);
      } else if (words[0].equals("<VirtualHost")) {
        key = null;
        value = null;
        String[] nextWords;
        String nextLine;
        while ((nextLine = br.readLine()) != null) {
          nextWords = nextLine.split("\\s");
          
          if (nextWords[0].equals("</VirtualHost>")) {
            break;
          } else if (nextWords[0].equals("DocumentRoot")) {
            value = nextWords[1];
          } else if (nextWords[0].equals("ServerName")) {
            key = nextWords[1];
          }

          if (key != null && value != null) {
            configMap.put(key,value);
          }


        }
      }
    }

    return configMap;


  }

}
