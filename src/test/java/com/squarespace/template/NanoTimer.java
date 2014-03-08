/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.template;

import java.util.Arrays;


public class NanoTimer {

  private static final int INITIAL_SIZE = 8;

  private final long[] times;

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
