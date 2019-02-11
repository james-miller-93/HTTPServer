/**
 * Monitor object for server heartbeat monitoring
 **/
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;

class Monitor1 implements Monitor{

  public Monitor1() {}

  public boolean canAccept() {
    //flip random coin
    int random = (int) (Math.random() * 2);
    if (random == 0) {
      return false;
    } else {
      return true;
    }
  }



}
