package com.squarespace.template;

import java.util.Arrays;


public class NanoTimer {

  private static int INITIAL_SIZE = 8;
  
  private long[] times;
  
  private int index;

  private long start;
  
  public NanoTimer() {
    this(INITIAL_SIZE);
  }
  
  public NanoTimer(int num) {
    times = new long[num];
  }
  
  public void start() {
    start = System.nanoTime();
  }
  
  public void stop() {
    if (start == -1) {
      throw new RuntimeException("stop() called before start()");
    }
    long elapsed = System.nanoTime() - start;
    if (index == times.length) {
      expand();
    }
    times[index] = elapsed;
    start = -1;
    index++;
  }

  public long[] getResults(boolean sorted) {
    if (index == 0) {
      return new long[] { };
    }
    long[] results = new long[index];
    System.arraycopy(times, 0, results, 0, index);
    if (sorted) {
      Arrays.sort(results);
    }
    return results;
  }
  
  private void expand() {
    int capacity = times.length * 2;
    long[] newTimes = new long[capacity];
    System.arraycopy(times, 0, newTimes, 0, index);
  }
  
}
