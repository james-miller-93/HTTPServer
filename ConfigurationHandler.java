/**
 * Read configuration files
 **/
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

class ConfigurationHandler {

  HashMap<String,String> configMap;
  int port, cacheSize, threadSize;
  String monitorString;

  public ConfigurationHandler(String configFile) throws Exception {
    
    HashMap<String,String> configureMap = new HashMap<String,String>();
    String key, value;

    BufferedReader br = new BufferedReader(new FileReader(configFile));
    String line;

    while((line = br.readLine()) != null) {
      String[] words = line.split("\\s");
      if (words[0].equals("Listen")) {
        port = Integer.parseInt(words[1]);
      } else if (words[0].equals("CacheSize")) {
        cacheSize = Integer.parseInt(words[1]);
      } else if (words[0].equals("ThreadPoolSize")) {
        threadSize = Integer.parseInt(words[1]);
      } else if (words[0].equals("Monitor")) {
        monitorString = words[1];
      } else if (words[0].equals("<VirtualHost")) {
        //System.out.println("Got here!");
        key = null;
        value = null;
        String[] nextWords;
        String nextLine;
        int j;
        
        boolean breakOut = false;
        while (!breakOut) {
          nextLine = br.readLine();
          nextWords = nextLine.split("\\s");

          for (j = 0; j < nextWords.length; j++) {
            //key = null;
            //value = null;
            //System.out.println(nextWords[j]);
          
          
            if (nextWords[j].equals("</VirtualHost>")) {
              breakOut = true;
              break;
            } else if (nextWords[j].equals("DocumentRoot")) {
              value = nextWords[j+1];
            } else if (nextWords[j].equals("ServerName")) {
              key = nextWords[j+1];
            }

            if (key != null && value != null) {
              //System.out.println("added following (key,value): (" + key + "," + value + ")");
              configureMap.put(key,value);
            }
          }

        }
      }
    }

    this.configMap = configureMap;



  }

  public HashMap<String,String> getConfigMap() {
    return this.configMap;
  }

  public int getPort() {
    return this.port;
  }

  public int getCacheSize() {
    return this.cacheSize;
  }

  public int getThreadSize() {
    return this.threadSize;
  }

  public String getMonitorString() {
    return this.monitorString;
  }

}
