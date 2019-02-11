/**
 * Cache object for server cache
 **/
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.util.concurrent.*;

class Cache {

  ConcurrentHashMap<String,byte[]> cacheMap;
  int size;
  int maxSize;
  boolean full;

  public Cache(int maximumSize) throws Exception {
    
    this.maxSize = maximumSize;
    this.size = 0;
    this.full = false;
    this.cacheMap = new ConcurrentHashMap<String,byte[]>();

  }

  void put(String key, byte[] value) {
    if (hasSpace(key,value)) {
    //if (!isFull()) {
      //this.cacheMap.put(key,value);
      
      int newSize = this.size + key.length() + value.length;

      if (newSize < 1000*this.maxSize) {
        this.cacheMap.put(key,value);
        this.size = newSize;
      } else {
        this.full = true;
      }
    }
  }

  boolean hasSpace(String key, byte[] value) {
    return (this.size + key.length() + value.length < this.maxSize);
  }

  boolean hasKey(String key) {
    return this.cacheMap.containsKey(key);
  }

  byte[] get(String key) {
    return this.cacheMap.get(key);
  }

  public ConcurrentHashMap<String,byte[]> getCacheMap() {
    return this.cacheMap;
  }

  public int getSize() {
    return this.size;
  }

  public int getMaxSize() {
    return this.maxSize;
  }

  public boolean isFull() {
    return this.full;
  }

}
