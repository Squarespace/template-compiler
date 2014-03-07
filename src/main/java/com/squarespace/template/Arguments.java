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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Parses arguments for Formatter/Predicate instances. It associates an argument delimiter with
 * the list of arguments, and lets plugins make simple assertions to ensure the correct number
 * of arguments were passed in.
 */
public class Arguments {
  
  private List<String> args = Collections.emptyList();

  private char delimiter = ' ';
  
  /** A place where a Predicate / Formatter can store data associated with the parsed args */
  private Object opaque;
  
  public Arguments() {
  }
  
  public Arguments(StringView raw) {
    parse(raw);
  }
  
  public String join() {
    StringBuilder buf = new StringBuilder();
    ReprEmitter.emit(this, false, buf);
    return buf.toString();
  }
  
  public String first() {
    return args.get(0);
  }
  
  public String get(int index) {
    return args.get(index);
  }
  
  public int count() {
    return args.size();
  }
  
  public boolean isEmpty() {
    return args.size() == 0;
  }
  
  public char getDelimiter() {
    return delimiter;
  }
  
  public List<String> getArgs() {
    return args;
  }
  
  /**
   * Associates an opaque object with the Arguments instance so it can be retrieved later.
   * Typically, you'll convert your arguments in the parsing phase, in order to report errors
   * early.  Then your plugin can fetch the already-converted arguments during the execute phase.
   */
  public void setOpaque(Object obj) {
    this.opaque = obj;
  }
  
  /**
   * @see #setOpaque(Object)
   */
  public Object getOpaque() {
    return this.opaque;
  }
  
  // Helper methods to make assertions about the args.

  public void exactly(int num) throws ArgumentsException {
    if (args.size() != num) {
      throw new ArgumentsException("Wrong number of args, exactly " + num + " expected");
    }
  }
  
  public void atMost(int num) throws ArgumentsException {
    between(0, num);
  }
  
  public void atLeast(int num) throws ArgumentsException {
    between(num, Integer.MAX_VALUE);
  }
  
  public void between(int min, int max) throws ArgumentsException {
    if (args.size() < min) {
      throw new ArgumentsException("Not enough args. At least " + min + " expected");
    }
    if (args.size() > max) {
      throw new ArgumentsException("Too many args. Takes between " + min + " and " + max);
    }
  }
  
  @Override
  public String toString() {
    return ReprEmitter.get(this, false);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Arguments) {
      Arguments other = (Arguments) obj;
      return delimiter == other.delimiter && args.equals(other.args);
    }
    return false;
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("Arguments does not implement hashCode()");
  }
  
  private void parse(StringView raw) {
    if (raw == null || raw.length() == 0) {
      return;
    }
    delimiter = raw.charAt(0);
    raw = raw.subview(1, raw.length());
    args = Arrays.asList(StringUtils.split(raw.repr(), delimiter));
  }
  
}
