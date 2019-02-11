/**
 * Yale CS433/533 Demo Basic Web Server
 **/
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

class WebRequestHandler {

    //static boolean _DEBUG = true;
    static boolean _DEBUG = false;
    static int     reqCount = 0;

    String WWW_ROOT;
    HashMap<String,String> serverMap;
    Socket connSocket;
    BufferedReader inFromClient;
    DataOutputStream outToClient;

    String urlName;
    String fileName;
    File fileInfo;
    File newFileInfo;

    String ifModifiedTime;

    String serverType;

    Monitor monitor;

    Cache cache;
    boolean outputFromCache = false;
    boolean outputFromCgi = false;
    InputStream programOutput;

    public WebRequestHandler(Socket connectionSocket, 
			     HashMap<String,String> serverMap, String serverType, Cache sharedCache, String monString) throws Exception
    {
        reqCount ++;

	    //this.WWW_ROOT = WWW_ROOT;
      this.serverMap = serverMap;
	    this.connSocket = connectionSocket;
      this.serverType = serverType;
      this.cache = sharedCache;

      if (monString.equals("Monitor1")) {
        monitor = new Monitor1();
      } else {
        monitor = new Monitor2();
      }

      
      outputFromCache = false;
      outputFromCgi = false;

	    inFromClient =
	      new BufferedReader(new InputStreamReader(connSocket.getInputStream()));

	    outToClient =
	      new DataOutputStream(connSocket.getOutputStream());

    }


    public void processRequest() 
    {

	try {
	    //DEBUG("Try to map url 2 file...");
      mapURL2File();
      //DEBUG("Mapped url 2 file");
	    if ( fileInfo != null || outputFromCache || outputFromCgi ) // found the file and knows its info
	    {
        if (outputFromCache) {
          //DEBUG("Found file in cache!!!");
        } else if (outputFromCgi) {
          DEBUG("CGI output");
        } else {
          DEBUG("Found the file!");
        }
        outputResponseHeader();
		    outputResponseBody();
        outputFromCache = false;
        outputFromCgi = false;
	    } // dod not handle error
      else {
        //DEBUG("Didn't find file or handle error");
      }
	    connSocket.close();
	} catch (Exception e) {
      System.out.println(e);
	    outputError(400, "Server error");
	}



    } // end of processARequest

    private void mapURL2File() throws Exception 
    {
				
	    
      
      String requestLine=null;
      String[] splitRequest;
      
      String serverName="";
      boolean mobile = false;
      
      while ((requestLine = inFromClient.readLine()) != null) {
        
        if (requestLine.equals("")) {
          break;
        }
    

        splitRequest = requestLine.split("\\s");
        //DEBUG(splitRequest[0]);
        //DEBUG("split request is: ");
        //for (int i = 0; i < splitRequest.length; i++) {
        //  DEBUG(splitRequest[i]);
        //}
        switch(splitRequest[0]) {
       
          case "GET":
            if (!splitRequest[2].equals("HTTP/1.0")) {
              outputError(500, "Bad request");
              DEBUG("third word was not HTTP/1.0:");
              return;
            }
            urlName = splitRequest[1];
            if (urlName.startsWith("/") == true ) {
              urlName = urlName.substring(1);
            }
            if (urlName.contains("..")) {
              outputError(501, "Invalid URL");
              return;
            }
            break;
      
          case "Host:":
            if (splitRequest.length != 2) {
              outputError(500, "Bad request");
              DEBUG("Host: line not two words, split req is: ");
              for (int j = 0; j < splitRequest.length; j++) {
                DEBUG(splitRequest[j]);
              }
              return;
            }
      
            serverName = splitRequest[1];
            break;
      
          case "User-Agent:":
            if (splitRequest.length < 2) {
              outputError(500, "Bad request");
              DEBUG("no word after User-Agent:");
              return;
            }
            for (int i = 0; i < splitRequest.length; i++) {
              if (splitRequest[i].contains("Android") || splitRequest[i].contains("iPhone")) {
                mobile = true;
              }
            }
            break;
      
          case "If-Modified-Since:":
            if (splitRequest.length != 7) {
              outputError(500, "Bad request");
              DEBUG("If modified line not correct format");
              return;
            }
            
            ifModifiedTime = splitRequest[1] + " " + splitRequest[2] + " " + splitRequest[3] + " " + splitRequest[4] + " " + splitRequest[5] + " " + splitRequest[6];

            break;
      
          default:
            DEBUG("different header");
            DEBUG(splitRequest[0]);
            if (splitRequest[0] == null || splitRequest[0].trim() == null) {
              requestLine = null;
            }
            break;
      
        }
      }
      

      //handle heartbeat monitoring
      if (urlName.equals("load")) {
        if (monitor.canAccept()) {
          fileInfo = null;
          outToClient.writeBytes("HTTP/1.0 200 OK\r\n");
          return;

        } else {
          outputError(503, "Server Overload");
          return;
        }

      }

      String docRoot = (String) serverMap.get(serverName);
      //String docRoot = "/home/accts/jm3255/Documents/Networks/HW3-HTTP";
      fileName = docRoot + "/" + urlName;

      //handle CGI
      String[] cgiSplit;
      String cgiQuery = null;
      if (fileName.indexOf("?") != -1) {
        cgiSplit = fileName.split("\\?");
        if (cgiSplit.length != 2) {
          outputError(500, "Bad Request");
          return;
        }
        fileName = cgiSplit[0];
        cgiQuery = cgiSplit[1];

      }


      String newFileName;

      //CHECK CACHE FIRST
      //need to check cache for fileName
      //and potentially need to check cache for
      //fileName + index.html

      //first check index/index_m.html cases
      if (fileName.endsWith("/")) {
        if (mobile) {
          newFileName = fileName + "index_m.html";
          if (this.cache.hasKey(newFileName)) {
            if (ifModifiedTime != null) {
              if (!ifModifiedSince(ifModifiedTime, new File( newFileName))) { 
                urlName = urlName + "index_m.html";
                fileName = newFileName;
                fileInfo = null;
                outputFromCache = true;
                return;
              }
           
            } else {
                urlName = urlName + "index_m.html";
                fileName = newFileName;
                fileInfo = null;
                outputFromCache = true;
                return;
            }
              
          } else {
            newFileName = fileName + "index.html";
            if (this.cache.hasKey(newFileName)) {
              if (ifModifiedTime != null) {
                if (!ifModifiedSince(ifModifiedTime, new File( newFileName))) { 
                  urlName = urlName + "index.html";
                  fileName = newFileName;
                  fileInfo = null;
                  outputFromCache = true;
                  return;
                }
              } else {
                  urlName = urlName + "index.html";
                  fileName = newFileName;
                  fileInfo = null;
                  outputFromCache = true;
                  return;
                }
            }
          }
        } else {
          newFileName = fileName + "index.html";
          if (this.cache.hasKey(newFileName)) {
            if (ifModifiedTime != null) {
              if (!ifModifiedSince(ifModifiedTime, new File( newFileName))) { 
                urlName = urlName + "index.html";
                fileName = newFileName;
                fileInfo = null;
                outputFromCache = true;
                return;
              }
            } else {
                urlName = urlName + "index.html";
                fileName = newFileName;
                fileInfo = null;
                outputFromCache = true;
                return;
              }

          }
        }
      }
      //else do normal check
      else {
        if (this.cache.hasKey(fileName)) {
          fileInfo = null;
          outputFromCache = true;
          return;
        }
      }


	    //DEBUG("Map to File name: " + fileName);

      //Search for file, it ends with /, check
      //index.html or index_m.html if mobile
      //if file not found, output error
      

      if (fileName.endsWith("/")) {
        if (mobile) {

          newFileName = fileName + "index_m.html";

          fileInfo = new File( newFileName );
          if ( !fileInfo.isFile() ) {
            newFileName = fileName + "index.html";
            fileInfo = new File( newFileName );
            if (!fileInfo.isFile() ) {
              outputError(404, "Not Found");
              fileInfo = null;
            } else {
              fileName = newFileName;
              urlName = urlName + "index.html";
            }
          } else {
            fileName = newFileName;
            urlName = urlName + "index_m.html";
          }
        } else {
          newFileName = fileName + "index.html";
          fileInfo = new File( newFileName );
          if ( !fileInfo.isFile() ) {
            outputError(404, "Not Found");
            fileInfo = null;
          } else {
            fileName = newFileName;
            urlName = urlName + "index.html";
          }
        }
      } else {
	      fileInfo = new File( fileName );
	      if ( !fileInfo.isFile() ) 
	      {
		      outputError(404,  "Not Found");
		      fileInfo = null;
	      }
      }
      
    
        //handle CGI
      String[] parameters = null;
      ProcessBuilder cgiProgram = new ProcessBuilder(fileName);
      Map<String,String> envVariables = null;


      if (fileInfo.canExecute()) {
        if (cgiQuery != null) {
          parameters = cgiQuery.split("\\+");
        }
        if (parameters != null) {
          String[] fullInput = new String[parameters.length + 1];
          fullInput[0] = fileName;
          for (int i = 0; i < parameters.length; i++) {
            fullInput[i + 1] = parameters[i];
          }
          cgiProgram = new ProcessBuilder(fullInput);
          //cgiProgram = new ProcessBuilder(fileName, parameters);
        }

        envVariables = cgiProgram.environment();

        if (cgiQuery != null) {
          envVariables.put("QUERY_STRING", cgiQuery);
        } else {
          envVariables.put("QUERY_STRING", "");
        }
        
        envVariables.put("REMOTE_HOST", this.connSocket.getRemoteSocketAddress().toString());
        envVariables.put("REMOTE_PORT", Integer.toString(this.connSocket.getPort()));
        envVariables.put("REQUEST_METHOD", "GET");
        envVariables.put("SERVER_ADDR", this.connSocket.getLocalSocketAddress().toString());
        envVariables.put("SERVER_PORT", Integer.toString(this.connSocket.getLocalPort()));
        envVariables.put("SERVER_PROTOCOL", "HTTP/1.0");
        envVariables.put("SERVER_SIGNATURE", "");
        envVariables.put("SERVER_ADMIN", "");
        envVariables.put("SERVER_SOFTWARE", "");
        envVariables.put("SERVER_NAME", serverName);


        Process executeProgram = cgiProgram.start();
        programOutput = executeProgram.getInputStream();

        outputFromCgi = true;
        return;
        

      }

    
      //Check if file was modified since IF-MODIFIED-DATE
      DEBUG("setting up sdf....");

      if (ifModifiedTime == null) {
        DEBUG("if mod time is null========");
        return;
      } else {
      
        DEBUG("about to check if modified...");
        if ( fileInfo != null ) {
          DEBUG("file was not null");
          if (fileInfo.isFile()) {
            DEBUG("ifModifiedTime: ");
            DEBUG(ifModifiedTime);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date ifMod = sdf.parse(ifModifiedTime);
            DEBUG(Long.toString(ifMod.getTime()));
            DEBUG("file info: ");
            DEBUG(Long.toString(fileInfo.lastModified()));
            DEBUG("if filemod - mod date, return false");
            
            if (ifModifiedSince(ifModifiedTime, fileInfo)) {
              fileInfo = null;
              outToClient.writeBytes("HTTP/1.0 304 Not Modified\r\n");
              DEBUG("Hasn't been modified!");
              return;
            }
          }
        }
        DEBUG("checked if modified");
      }


    } // end mapURL2file

    private String formatDate() {
      Calendar calendar = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      return sdf.format(calendar.getTime());
    }

    private boolean ifModifiedSince(String ifModTime, File checkFile) throws Exception{
        SimpleDateFormat sDateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date ifModDate = sDateFormat.parse(ifModTime);
      
        if (checkFile.lastModified() - ifModDate.getTime() < 0) {
          return false;
        } else {
          return true;
        }
    }


    private void outputResponseHeader() throws Exception 
    {
	    //outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n");
	    outToClient.writeBytes("HTTP/1.0 200 OK\r\n");
	    //outToClient.writeBytes("Set-Cookie: MyCool433Seq12345\r\n");i
      outToClient.writeBytes("Date: " + formatDate() + "\r\n");
      outToClient.writeBytes("Server: " + serverType + "\r\n");

	    if (urlName.endsWith(".jpg"))
	        outToClient.writeBytes("Content-Type: image/jpeg\r\n");
	    else if (urlName.endsWith(".gif"))
	        outToClient.writeBytes("Content-Type: image/gif\r\n");
	    else if (urlName.endsWith(".html") || urlName.endsWith(".htm"))
	        outToClient.writeBytes("Content-Type: text/html\r\n");
	    else
	        outToClient.writeBytes("Content-Type: text/plain\r\n");
    }

    private void outputResponseBody() throws Exception 
    {

	    //int numOfBytes = (int) fileInfo.length();
	    
      int numOfBytes;
      byte[] fileInBytes;
      
      //send file bytes
      if (outputFromCache) {
        //DEBUG("try to read from cache==========");
        fileInBytes = this.cache.get(fileName);
        //DEBUG("read from the cache########################");
        numOfBytes = fileInBytes.length;
      } else if (outputFromCgi) {
        //DEBUG("try output from cgi!!");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte curByte;
        while((curByte = (byte) programOutput.read()) > 0) {
          baos.write(curByte);
        }
        //programOutput.read(baos);
        fileInBytes = baos.toByteArray();
        numOfBytes = fileInBytes.length;
        //DEBUG("output from cgi!");
      } else {

        numOfBytes = (int) fileInfo.length();
	      FileInputStream fileStream  = new FileInputStream (fileName);
	
	      fileInBytes = new byte[numOfBytes];
	      fileStream.read(fileInBytes);

        if (cache.hasSpace(fileName, fileInBytes) && !outputFromCgi) {
          this.cache.put(fileName, fileInBytes);
          //DEBUG("added to the cache!!=====!!!=====!!");
        }
      }

      outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
	    outToClient.writeBytes("\r\n");
	    outToClient.write(fileInBytes, 0, numOfBytes);
    }

    void outputError(int errCode, String errMsg)
    {
	    try {
	        outToClient.writeBytes("HTTP/1.0 " + errCode + " " + errMsg + "\r\n");
	    } catch (Exception e) { 
        System.out.println(e);
        //System.out.println(errCode);
        //System.out.println(errMsg);
      }
    }

    static void DEBUG(String s) 
    {
       if (_DEBUG)
          System.out.println( s );
    }
}
