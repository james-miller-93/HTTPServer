/**
 * Monitor interface for server heartbeat monitoring
 **/
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;

interface Monitor {
  boolean canAccept();
}
