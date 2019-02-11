# HTTPServer
This reposity contains an implementation of a server using a basic version of HTTP 1.0. This implementation was completed while taking Yale University Computer Science course 533, Networks. Specifically, his repository contains implementations of a sequential server, a per thread server, and various thread pool servers that handle HTTP 1.0 GET requests. For a detailed specification of the implementations see:

http://zoo.cs.yale.edu/classes/cs433/cs433-2018-fall/assignments/assign3/index.html

http://zoo.cs.yale.edu/classes/cs433/cs433-2018-fall/assignments/assign3/index2.html

HTTP PROTOCOL AND WEB SERVER DESIGN

AUTHOR: JAMES MILLER

=============================== Summary of Code files ===============================

This set of files constructs a sequential server, per thread
server, and various thread pool servers that handle HTTP 1.0
GET requests. The majority of the functionality for this servers
can be found in the WebRequestHandler.java file. Each of these
servers accept connections and then use this class to process
the requests. Following the terminology of Yale CS 433/533
Homework sheet 3 (part 1), each of these servers can handle
configuration files (ConfigurationHandler.java), HTTP GET
method, Headers (inc. If-Modified-Since and User-Agent),
URL Mapping, caching, CGI, and heartbeat monitoring. I completed
each of the thread pool servers.

==================================================================================

=========================== How to Compile Files ================================

Compile in the following order:

- javac Monitor.java
- javac Monitor1.java
- javac Monitor2.java
- javac Cache.java
- javac ConfigurationHandler.java
- javac WebRequestHandler.java
- javac SHTTPTestClient.java
- javac SequentialServer.java
- javac ThreadWebRequestHandler.java
- javac PerThreadServer.java
- javac ThreadPoolServer1.java
- javac ThreadPoolServer2.java
- javac ThreadPoolServer3.java

=============================================================================

========================== How to Run Client/Servers ==========================

Run the client and server in separate terminals. The same client
will be run each time:

java SHTTPTestClient -server [server] -servname [server name] -port [server port] -parallel [# threads] -files [file name] -T [time in seconds]

-file name: name of file which contains list of files to be requested from server

Each of the servers can be run using the following command:

java [server type] -config [configuration file]

-server type: the type of server you wish to run
-configuration file: apache style configuration file

for example:

java PerThreadServer -config real-config.conf

============================ Brief Description of Files ==========================

WebRequestHandler.java: this is the majority of the functionality
of all of the servers. This code implements processing HTTP GET
requests, URL mapping, caching, headers (if-modified and user-agents),
CGI, and heartbeat monitoring.

SequentialServer.java: this code implements the sequential server.
Upon accepting a connection, WebRequestHandler is called to process
the request.

PerThreadServer.java: this code implements the per thread server.
Upon accepting connections, the server initiates a new thread for
each connection and handles the request using ThreadWebRequestHandler.java

ThreadWebRequestHandler.java: this class implements WebRequestHandler as
a runnable process, so that it can be called by a thread in
PerThreadServer.

ThreadPoolServer1.java: this code implements the thread pool server
with service threads competing on welcome socket. This class also
implements a ThreadPoolHandler1 class, which uses WebRequestHandler
to process the request.

ThreadPoolServer2.java: this code implements the thread pool server
with busy wait. This class also implements a ThreadPoolHandler2 class,
which uses WebRequestHandler to process requests.

ThreadPoolServer3.java: this code implements the thread pool server
with a shared queue and suspension. This class also implements a
ThreadPoolHandler3 class, which uses WebRequestHandler to process requests.

SHTTPTestClient.java: this code implements the client used to benchmark
the performance of the various servers. Upon parsing command line arguments,
this client repeatedly requests the server for various files for a designated
time interval. Once the time interval is completed, the client outputs
the total transaction throughput, data rate throughput (bytes per sec), and
the average wait time (in ms).

ConfigurationHandler.java: this class allows for parsing of configuration
files. The class obtains (if included) the listen port, cache size,
thread pool size, and heartbeat monitor. The class also creates a hashmap
to connect virtual server names to their corresponding document roots.
This is instantiated by all of the servers to set their parameters based
on the input config file.

Cache.java: this class implements the cache object used by the servers.
The cache contains integers size and maxSize for the current and maximum
sizes (max comes from config file). The object also stores a map that serves
as the actual cache for the data. The class checks if the cache has room
before adding more files.

Monitor.java: this is a very basic interface for building heartbeat monitors
for the servers. Its only method is canAccept(), which returns a boolean.

Monitor1.java: this implements a Monitor interface. This canAccept() method
returns true or false randomly.

Monitor2.java: this implements a Monitor interface. This canAccept() method
always returns false.

=============================================================================

======================= Testing Functionalities =============================

In order to test the functionality of my servers, I used various different
inputs and verified the correct behavior of the servers.

URL-Mapping/GET Request: upon sending GET requests to all of my servers
using SHTTPTestClient along with files.txt, each of the servers sent
back the corresponding files specified in the files.txt (outputs received
from servers were printed by client)

Headers-If Modified: I adjusted the client to send a GET request that
also included in the header If-Modified-Since; MON, 27 SEP 2010 00:00:00 GMT
and then If-Modified-Since: current time, and the server replied correctly.

Headers-User-Agent: I adjusted the client to query the servers with different
user agents, and when the agent contains the substring iphone or android,
the server responds properly. Also, if the client asks for a file path ending
with "/", the server responds with index.html (if available).

Caching: I used print statements to ensure that the code was actually
accessing the cache object and adding into/ reading from this object.

CGI: I downloaded the example CGI file and queried for this file from
the client. The server then output the same output as found online,
so I was confident that it was working.

Heartbeat Monitoring: I queried the server with URL as /load, and
the server properly responds according to the algorithm specified
by the Monitor objects.

==========================================================================
