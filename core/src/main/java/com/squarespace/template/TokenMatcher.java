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

import java.util.regex.Matcher;


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

  private final Matcher matcherArgs;

  private final Matcher matcherBooleanOp;

  private final Matcher matcherFormatter;

  private final Matcher matcherKeyword;

  private final Matcher matcherLocalVariable;

  private final Matcher matcherPath;

  private final Matcher matcherPredicate;

  private final Matcher matcherPredicateArgs;

  private final Matcher matcherVariable;

  private final Matcher matcherWhitespace;

  private final Matcher matcherWordSection;

  private final Matcher matchWordWith;

  private boolean matched;

  public TokenMatcher(final String raw) {
    this.raw = raw;
    this.matcherArgs = Patterns.ARGUMENTS.matcher(raw);
    this.matcherBooleanOp = Patterns.BOOLEAN_OP.matcher(raw);
    this.matcherFormatter = Patterns.FORMATTER.matcher(raw);
    this.matcherKeyword = Patterns.KEYWORD.matcher(raw);
    this.matcherLocalVariable = Patterns.LOCAL_VARIABLE.matcher(raw);
    this.matcherPath = Patterns.PATH.matcher(raw);
    this.matcherPredicate = Patterns.PREDICATE.matcher(raw);
    this.matcherPredicateArgs = Patterns.PREDICATE_ARGUMENTS.matcher(raw);
    this.matcherVariable = Patterns.VARIABLE.matcher(raw);
    this.matcherWhitespace = Patterns.WHITESPACE.matcher(raw);
    this.matcherWordSection = Patterns.WORD_SECTION.matcher(raw);
    this.matchWordWith = Patterns.WORD_WITH.matcher(raw);
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
    return match(matcherArgs);
  }

  public boolean formatter() {
    return match(matcherFormatter);
  }

  public boolean keyword() {
    return match(matcherKeyword);
  }

  public boolean localVariable() {
    return match(matcherLocalVariable);
  }

  public boolean operator() {
    return match(matcherBooleanOp);
  }

  public boolean path() {
    return match(matcherPath);
  }


  public boolean pipe() {
    return match('|');
  }

  public boolean predicate() {
    return match(matcherPredicate);
  }

  public boolean predicateArgs() {
    return match(matcherPredicateArgs);
  }

  public boolean space() {
    return match(' ');
  }

  public boolean variable() {
    return match(matcherVariable);
  }

  public boolean whitespace() {
    return match(matcherWhitespace);
  }

  public boolean wordSection() {
    return match(matcherWordSection);
  }

  public boolean wordWith() {
    return match(matchWordWith);
  }

  /**
   * Perform a match using a Matcher and, if successful, set the match range.
   */
  private boolean match(Matcher matcher) {
    matcher.region(pointer, end);
    matched = matcher.lookingAt();
    if (matched) {
      matchStart = pointer;
      matchEnd = matcher.end();
    }
    return matched;
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
