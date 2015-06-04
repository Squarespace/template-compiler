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
 * Holds arguments for Formatter/Predicate instances.
 *
 * It associates an argument delimiter with the list of arguments, and defines
 * methods allowing plugins to assert that the correct number of arguments are
 * present.
 */
public class Arguments {

  /**
   * Raw arguments.
   */
  private List<String> args = Collections.emptyList();

  /**
   * Argument delimiter character.
   */
  private char delimiter = ' ';

  /**
   * A place where a Predicate / Formatter can attach data to the parsed arguments.
   */
  private Object opaque;

  /**
   * Constructs an empty argument set.
   */
  public Arguments() {
  }

  /**
   * Constructs arguments by parsing the given raw string.
   */
  public Arguments(StringView raw) {
    parse(raw);
  }

  /**
   * Returns a string with the arguments joined using the delimiter.
   */
  public String join() {
    StringBuilder buf = new StringBuilder();
    ReprEmitter.emit(this, false, buf);
    return buf.toString();
  }

  /**
   * Returns the first argument.
   */
  public String first() {
    return args.get(0);
  }

  /**
   * Returns the Nth argument.
   */
  public String get(int index) {
    return args.get(index);
  }

  /**
   * Returns the number of arguments.
   */
  public int count() {
    return args.size();
  }

  /**
   * Indicates whether the argument list is empty.
   */
  public boolean isEmpty() {
    return args.isEmpty();
  }

  /**
   * Returns the delimiter character.
   */
  public char getDelimiter() {
    return delimiter;
  }

  /**
   * Returns the arguments.
   */
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

  /**
   * Asserts there are exactly {@code num} arguments. Throws an exception otherwise.
   */
  public void exactly(int num) throws ArgumentsException {
    if (args.size() != num) {
      throw new ArgumentsException("Wrong number of args, exactly " + num + " expected");
    }
  }

  /**
   * Asserts there are at most {@code num} arguments. Throws an exception otherwise.
   */
  public void atMost(int num) throws ArgumentsException {
    between(0, num);
  }

  /**
   * Asserts there are at least {@code num} arguments. Throws an exception otherwise.
   */
  public void atLeast(int num) throws ArgumentsException {
    between(num, Integer.MAX_VALUE);
  }

  /**
   * Asserts there are between {@code min} and {@code max} arguments, inclusive.
   * Throws an exception otherwise.
   */
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

  /**
   * Parses the raw string into a list of arguments.
   */
  private void parse(StringView raw) {
    if (raw == null || raw.length() == 0) {
      return;
    }
    delimiter = raw.charAt(0);
    raw = raw.subview(1, raw.length());
    args = Arrays.asList(StringUtils.split(raw.repr(), delimiter));
  }

}
