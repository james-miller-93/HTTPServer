/**
 * Yale CS433/533 Thread Based Web Server
 **/
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

class ThreadWebRequestHandler implements Runnable {

  WebRequestHandler wrh;

  public ThreadWebRequestHandler( Socket connectionSocket, HashMap<String,String> serverMap, String serverType, Cache sharedCache, String monString) throws Exception {

    WebRequestHandler webHandler = new WebRequestHandler(connectionSocket, serverMap, serverType, sharedCache, monString);
    this.wrh = webHandler;
  }

  public void run() {
    this.wrh.processRequest();
  }
}


