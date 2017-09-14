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

import com.squarespace.compiler.match.Recognizers.Recognizer;


/**
 * Incremental pattern matcher.
 */
public class TokenMatcher {

  private final String raw;

  /** Start as set by region() */
  private int start = -1;

  /** Start of the most recent match. */
  private int matchStart = -1;

  /** End of the most recent match. */
  private int matchEnd = -1;

  /** Pointer to the character range of potential matches */
  private int pointer = -1;

  /** End of the character range of potential matches */
  private int end = -1;

  private boolean matched;

  public TokenMatcher(final String raw) {
    this.raw = raw;
    region(0, raw.length());
  }

  /**
   * Set the region of the potential matches.
   */
  public TokenMatcher region(int start, int end) {
    this.start = start;
    this.pointer = start;
    this.end = end;
    return this;
  }

  /**
   * Return a view of the remainder of the potential character range. Useful when
   * a match fails to report it, e.g. "expected FOO found 'BAR'".
   */
  public StringView remainder() {
    return new StringView(raw, pointer, end);
  }

  /**
   * Consume the matched range, returning a token containing all the characters matched.
   * Move the match pointer past the matched range.
   */
  public StringView consume() {
    StringView tok = new StringView(raw, matchStart, matchEnd);
    pointer = matchEnd;
    return tok;
  }

  /**
   * Once we're done matching we can assert that we've consumed the entire input range.
   */
  public boolean finished() {
    return pointer == end;
  }

  /**
   * Index of the start of the most recent match.
   */
  public int matchStart() {
    return matchStart;
  }

  /**
   * Index of the end of the most recent match.
   */
  public int matchEnd() {
    return matchEnd;
  }

  /**
   * Index of the pointer to the next character in the remainder of the input range.
   */
  public int pointer() {
    return pointer;
  }

  public int start() {
    return start;
  }

  /**
   * Index of the end of the input range.
   */
  public int end() {
    return end;
  }

  public boolean arguments() {
    return match(Patterns.ARGUMENTS);
  }

  public boolean formatter() {
    return match(Patterns.FORMATTER);
  }

  public boolean keyword() {
    return match(Patterns.RESERVED_WORD);
  }

  public boolean localVariable() {
    return match(Patterns.VARIABLE_DEFINITION);
  }

  public boolean operator() {
    return match(Patterns.BOOLEAN_OP);
  }

  public boolean path() {
    return match(Patterns.PATH);
  }

  public boolean pipe() {
    return match('|');
  }

  public boolean predicate() {
    return match(Patterns.PREDICATE);
  }

  public boolean predicateArgs() {
    return match(Patterns.PREDICATE_ARGUMENTS);
  }

  public boolean space() {
    return match(' ');
  }

  public boolean variable() {
    return match(Patterns.VARIABLE_REF_DOTTED);
  }

  public boolean variablesDelimiter() {
    return match(Patterns.VARIABLES_DELIMITER);
  }

  public boolean whitespace() {
    return match(Patterns.WHITESPACE);
  }

  public boolean wordSection() {
    return match(Patterns.KEYWORD_SECTION);
  }

  public boolean wordWith() {
    return match(Patterns.KEYWORD_WITH);
  }

  public boolean peek(int skip, char ch) {
    int p = pointer + skip;
    return pointer < end ? raw.charAt(p) == ch : false;
  }

  /**
   * Perform a match using a Recognizer pattern and, if successful,
   * set the match range.
   */
  private boolean match(Recognizer pattern) {
    int pos = pattern.match(raw, pointer, end);
    if (pos > pointer) {
      matchStart = pointer;
      matchEnd = pos;
      return true;
    }
    return false;
  }

  /**
   * Perform a match of a single character and, if successful, set the match range.
   */
  private boolean match(char ch) {
    if (pointer == end) {
      return false;
    }
    matched = raw.charAt(pointer) == ch;
    if (matched) {
      matchStart = pointer;
      matchEnd = pointer + 1;
    }
    return matched;
  }


}
