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
  
  private final Matcher match_ARGS;

  private final Matcher match_BOOLEAN_OP;
  
  private final Matcher match_FORMATTER;

  private final Matcher match_KEYWORD;
  
  private final Matcher match_PREDICATE;
  
  private final Matcher match_VARIABLE;
  
  private final Matcher match_WHITESPACE;
  
  private final Matcher match_WORD_SECTION;
  
  private final Matcher match_WORD_WITH;

  private boolean matched;

  public TokenMatcher(final String raw) {
    this.raw = raw;
    this.match_ARGS = Patterns.ARGUMENTS.matcher(raw);
    this.match_BOOLEAN_OP = Patterns.BOOLEAN_OP.matcher(raw);
    this.match_FORMATTER = Patterns.FORMATTER.matcher(raw);
    this.match_KEYWORD = Patterns.KEYWORD.matcher(raw);
    this.match_PREDICATE = Patterns.PREDICATE.matcher(raw);
    this.match_VARIABLE = Patterns.VARIABLE.matcher(raw);
    this.match_WHITESPACE = Patterns.WHITESPACE.matcher(raw);
    this.match_WORD_SECTION = Patterns.WORD_SECTION.matcher(raw);
    this.match_WORD_WITH = Patterns.WORD_WITH.matcher(raw);
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
    return match(match_ARGS);
  }
  
  public boolean formatter() {
    return match(match_FORMATTER);
  }
  
  public boolean keyword() {
    return match(match_KEYWORD);
  }

  public boolean operator() {
    return match(match_BOOLEAN_OP);
  }

  public boolean pipe() {
    return match('|');
  }
  
  public boolean predicate() {
    return match(match_PREDICATE);
  }

  public boolean space() {
    return match(' ');
  }
  
  public boolean variable() {
    return match(match_VARIABLE);
  }
  
  public boolean whitespace() {
    return match(match_WHITESPACE);
  }

  public boolean wordSection() {
    return match(match_WORD_SECTION);
  }
  
  public boolean wordWith() {
    return match(match_WORD_WITH);
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
