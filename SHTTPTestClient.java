// SHTTPTestClient.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.text.*;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.*;

/* 
    * Client to send test http requests to server.
*/
public class SHTTPTestClient {

    static String server, servname, files;
    static int port, parallel, time;
    static Thread threadArray[];
    static InetAddress servAddress;
    
    public static void main(String[] args) throws Exception {
        // Get command line argument.
        if (args.length != 12) {
            System.out.println("Usage: -server <server> -servname <servname> -port <server port> -parallel <# of threads> -files <file name> -T <time of test in seconds>");

            return;
        }
        
        // Parse command line arguments
        for (int i = 0; i < 12; i++) {
          if (args[i].equals("-server")) {
            server = args[i+1];
          } else if (args[i].equals("-servname")) {
            servname = args[i+1];
          } else if (args[i].equals("-port")) {
            port = Integer.parseInt(args[i+1]);
          } else if (args[i].equals("-parallel")) {
            parallel = Integer.parseInt(args[i+1]);
          } else if (args[i].equals("-files")) {
            files = args[i+1];
          } else if (args[i].equals("-T")) {
            time = Integer.parseInt(args[i+1]);
          }
        }

        servAddress = InetAddress.getByName(server);

        threadArray = new Thread[parallel];

        AtomicLong totalFiles = new AtomicLong();
        AtomicLong totalBytes = new AtomicLong();
        AtomicLong totalHits = new AtomicLong();
        AtomicLong totalDelay = new AtomicLong();

        Runnable executeThread = new Runnable() {
          public void run() {

            try {
              Socket sock;
              DataOutputStream outToServer;
              String fileArray[] = getFiles(files);
              BufferedReader inFromServer;
              long startTime;
              startTime = System.currentTimeMillis();


              while((System.currentTimeMillis() - startTime) < time*1000) {

                long sendTime, recTime;
                for (int j = 0; j < fileArray.length; j++) {
                  sock = new Socket(server, port);
                  //sock = new Socket(servAddress, port);
                  outToServer = new DataOutputStream(sock.getOutputStream());
                  sendTime = System.currentTimeMillis();
                  outToServer.writeBytes(formGetRequest(fileArray[j], servname));
                  //outToServer.writeBytes(formGetRequest(fileArray[j], servname, formatCurDate()));
              
                  
                  
                  try {
                    inFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    recTime = System.currentTimeMillis();
                    totalFiles.addAndGet(1);
                    totalDelay.addAndGet(recTime - sendTime);
                    String lineFromServer;
                    while ((lineFromServer = inFromServer.readLine()) != null) {
                      //System.out.println(lineFromServer);
                      totalBytes.addAndGet(lineFromServer.length());
                    }
                  } catch(Exception e) {
                    //System.out.println(e);
                  }
                 // String lineFromServer;
                 // for (int l=0; l < 8; l++) {
                 //   lineFromServer = inFromServer.readLine();
                 //   System.out.println(lineFromServer);
                 // }
                
                  //String lineFromServer;
                  //while ((lineFromServer = inFromServer.readLine()) != null) {
                  //  System.out.println(lineFromServer);
                  //}

                  sock.close();
                }

              }

     
            } catch(Exception e) {
              System.out.println(e);
            }
          }
        };

           


          for (int k=0; k < threadArray.length; k++) {
            threadArray[k] = new Thread(executeThread);
            threadArray[k].start();
          }

          for (int j=0; j < threadArray.length; j++) {
            threadArray[j].join();
          }

          System.out.println("Files per second: ");
          System.out.println(totalFiles.get()/time);
          System.out.println("Total bytes: ");
          System.out.println(totalBytes.get());
          System.out.println("Bytes per second: ");
          System.out.println(totalBytes.get()/time);
          System.out.println("Average wait time: ");
          System.out.println(((double) totalDelay.get())/((double) totalFiles.get()));

        //TimerTask task = new KillThreads();
        //Timer timer = new Timer(true);
        //timer.schedule(task, time*1000);
    }


   public static String formGetRequest(String file, String servername) {
     String message = "GET " + file + " HTTP/1.0\r\n" + "Host: " + servername + "\r\n" + "\r\n" + null;
     return message;
   }
   
   public static String formGetRequest(String file, String servername, String ifModSince) {
     String message = "GET " + file + " HTTP/1.0\r\n" + "Host: " + servername + "\r\n" + "If-Modified-Since: " + "MON, 27 SEP 2010 00:00:00 GMT" + "\r\n" + "\r\n" + null;
     return message;
   }

    private static String formatCurDate() {
      Calendar calendar = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      return sdf.format(calendar.getTime());
    }


   public static String[] getFiles(String files) throws Exception {

     BufferedReader br = new BufferedReader(new FileReader(files));
     String file;
     int numFiles = 0;
     while((file = br.readLine()) != null) {
       numFiles++;
     }
        
     String fileStrings[] = new String[numFiles];
     BufferedReader br2 = new BufferedReader(new FileReader(files));
     int i = 0;

     while((file = br2.readLine()) != null) {
       fileStrings[i] = file;
       i++;
     }

     return fileStrings;
   }

   public static void executeThread(Thread thread) throws Exception{

     Socket sock = new Socket(servAddress, port);
     DataOutputStream outToServer = new DataOutputStream(sock.getOutputStream());
     String fileArray[] = getFiles(files);

     while(true) {

       for (int j = 0; j < fileArray.length; j++) {
         outToServer.writeBytes(formGetRequest(fileArray[j], servname));
       }

     }
     
   }

   public static class KillThreads extends TimerTask {

     public void run() {

       for (int k=0; k < threadArray.length; k++) {
         threadArray[k].interrupt();
       }
     }
   }
}

