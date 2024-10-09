/**
 * Copyright, 2017, Squarespace, Inc.
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

package com.squarespace.template.match;


import static com.squarespace.template.match.DefaultCharClassifier.DASH;
import static com.squarespace.template.match.DefaultCharClassifier.DIGIT;
import static com.squarespace.template.match.DefaultCharClassifier.HEXDIGIT;
import static com.squarespace.template.match.DefaultCharClassifier.LOWERCASE;
import static com.squarespace.template.match.DefaultCharClassifier.UNDERSCORE;
import static com.squarespace.template.match.DefaultCharClassifier.UPPERCASE;


/**
 * Simple pattern recognizer state machines.
 *
 * Recognizers constructed by this class return the ending position of the
 * match, or FAIL for no match.
 *
 * Faster than equivalent java.util.regex regular expressions due to lower overhead:
 *
 *  - recognizers are optimized for the patterns we care about
 *  - recognizers have fewer variables to update
 *  - no need for a separate java.util.regex.Matcher object
 *  - no region() or reset() calls required when matching against a
 *    different string or position.
 */
public class Recognizers {

  /**
   * Indicates a failure to match a valid position in the string.
   */
  public static final int FAIL = -1;

  private static final Recognizer ANY = new Any();

  private static final Recognizer WHITESPACE = new Whitespace(false);

  private static final Recognizer NOT_WHITESPACE = new Whitespace(true);

  private static final CharClassifier CLASSIFIER = new DefaultCharClassifier();

  private Recognizers() {
  }

  public static Recognizer any() {
    return ANY;
  }

  public static Recognizer cardinality(Recognizer pattern, int minimum, int maximum) {
    return new Cardinality(pattern, minimum, maximum);
  }

  public static Recognizer characters(char first, char... characters) {
    return new Characters(false, first, characters);
  }

  public static Recognizer charClass(int bitmask, CharClassifier classifier) {
    return new CharacterClass(bitmask, classifier);
  }

  public static Recognizer charRange(char start, char end) {
    return new CharacterRange(start, end);
  }

  public static Recognizer choice(Recognizer... patterns) {
    return new Recognizers.Choice(patterns);
  }

  public static Recognizer decimal() {
    return new Recognizers.Decimal();
  }

  public static Recognizer digit() {
    return charClass(DIGIT, CLASSIFIER);
  }

  public static Recognizer digits() {
    return oneOrMore(digit());
  }

  public static Recognizer hexdigit() {
    return charClass(HEXDIGIT, CLASSIFIER);
  }

  public static Recognizer literal(String str) {
    return new Literal(str);
  }

  public static Recognizer lookAhead(Recognizer pattern) {
    return new LookAhead(pattern);
  }

  public static Recognizer oneOrMore(Recognizer pattern) {
    return new Plus(pattern);
  }

  public static Recognizer notAscii() {
    return notCharRange('\u0000', '\u009f');
  }

  public static Recognizer notCharacters(char first, char... characters) {
    return new Characters(true, first, characters);
  }

  public static Recognizer notCharClass(int bitmask, CharClassifier classifier) {
    return new CharacterClass(bitmask, true, classifier);
  }

  public static Recognizer notCharRange(char start, char end) {
    return new CharacterRange(true, start, end);
  }

  public static Recognizer notHexdigit() {
    return notCharClass(HEXDIGIT, CLASSIFIER);
  }

  public static Recognizer sequence(Recognizer... patterns) {
    return new Recognizers.Sequence(patterns);
  }

  public static Recognizer whitespace() {
    return WHITESPACE;
  }

  public static Recognizer notWhitespace() {
    return NOT_WHITESPACE;
  }

  public static Recognizer word() {
    return charClass(LOWERCASE | UPPERCASE | DIGIT | UNDERSCORE, CLASSIFIER);
  }

  public static Recognizer worddash() {
    return charClass(LOWERCASE | UPPERCASE | DIGIT | UNDERSCORE | DASH, CLASSIFIER);
  }

  public static Recognizer zeroOrOne(Recognizer pattern) {
    return new QuestionMark(pattern);
  }

  public static Recognizer zeroOrMore(Recognizer pattern) {
    return new Star(pattern);
  }

  public static Recognizer zeroOrMore(Recognizer pattern, int limit) {
    return new Cardinality(pattern, limit);
  }

  /**
   * Matches any character.
   */
  static class Any implements Recognizer {
    @Override
    public int match(CharSequence seq, int pos, int length) {
      return (pos < length) ? pos + 1 : FAIL;
    }
  }

  /**
   * Matches zero or more of the child matcher.
   */
  static class Cardinality implements Recognizer {

    private final Recognizer pattern;

    private final int start;

    private final int limit;

    Cardinality(Recognizer pattern, int limit) {
      this(pattern, 0, limit);
    }

    Cardinality(Recognizer pattern, int start, int limit) {
      this.pattern = pattern;
      this.start = start;
      this.limit = limit;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      int result = pos;
      int count = 0;
      while (true) {
        pos = pattern.match(seq, pos, length);
        if (pos == FAIL) {
          break;
        }
        count++;
        result = pos;
        if (count == limit) {
          break;
        }
      }
      return start > 0 ? (count < start ? FAIL : result) : result;
    }
  }

  /**
   * Match any of a list of characters.
   */
  static class Characters implements Recognizer {

    private final boolean invert;

    private final char first;

    private final char[] chars;

    Characters(boolean invert, char first, char... chars) {
      this.invert = invert;
      this.first = first;
      this.chars = chars;
    }

    @Override
    public int match(CharSequence seq, int pos, int len) {
      if (pos < len) {
        char actual = seq.charAt(pos);
        if (actual == first) {
          return invert ? FAIL : pos + 1;
        }
        for (char expected : chars) {
          if (actual == expected) {
            return invert ? FAIL : pos + 1;
          }
        }
        // Nothing matched, so inverse succeeds.
        return invert ? pos + 1 : FAIL;
      }
      return FAIL;
    }
  }

  /**
   * Matches if the character is a member of the given character class.
   */
  static class CharacterClass implements Recognizer {

    private final int bitmask;

    private final boolean invert;

    private final CharClassifier classifier;

    CharacterClass(int charClass, CharClassifier classifier) {
      this(charClass, false, classifier);
    }

    CharacterClass(int charClass, boolean invert, CharClassifier classifier) {
      this.bitmask = charClass;
      this.invert = invert;
      this.classifier = classifier;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      if (pos < length) {
        boolean result = this.classifier.isMember(seq.charAt(pos), bitmask);
        return invert ? (result ? FAIL : pos + 1) : (result ? pos + 1 : FAIL);
      }
      return FAIL;
    }

  }

  /**
   * Returns next position if the character is within the given range.
   * Setting the invert flag, the match will succeed if the character is not
   * within the given range.
   */
  static class CharacterRange implements Recognizer {

    private final boolean invert;

    private final char start;

    private final char end;

    CharacterRange(char start, char end) {
      this(false, start, end);
    }

    CharacterRange(boolean invert, char start, char end) {
      this.invert = invert;
      this.start = start;
      this.end = end;
    }

    @Override
    public int match(CharSequence seq, int pos, int len) {
      if (pos < len) {
        char ch = seq.charAt(pos);
        boolean result = invert ? (ch < start || ch > end) : (ch >= start && ch <= end);
        return result ? pos + 1 : FAIL;
      }
      return FAIL;
    }

  }

  /**
   * Return result from first matcher that matches.
   */
  static class Choice implements Recognizer {

    private final Recognizer[] patterns;

    Choice(Recognizer[] patterns) {
      this.patterns = patterns;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      int save = FAIL;
      for (Recognizer pattern : patterns) {
        // Try choices until we see one that advances past current position.
        int res = pattern.match(seq, pos, length);
        if (res > save) {
          save = Math.max(save, res);
        }
      }
      return save;
    }
  }

  /**
   * Optimized to match decimal with zero or more leading digits and
   * one optional decimal point. Must have at least 1 digit to pass,
   * regardless if decimal is leading or trailing.
   */
  static class Decimal implements Recognizer {

    @Override
    public int match(CharSequence seq, int pos, int length) {
      int save = pos;
      int res = FAIL;
      boolean dot = false;
      while (pos < length) {
        char ch = seq.charAt(pos);
        if (ch == '.') {
          if (dot) {
            break;
          }
          dot = true;

        } else if (!CLASSIFIER.isMember(ch, DIGIT)) {
          break;
        }
        pos++;
        res = pos;
      }
      return (dot && (save == pos - 1)) ? FAIL : res;
    }

  }

  /**
   * Match an entire literal string.
   */
  static class Literal implements Recognizer {

    private final String literal;

    private final int literalLength;

    Literal(String value) {
      this.literal = value;
      this.literalLength = literal.length();
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      if (literalLength <= (length - pos)) {
        for (int i = 0; i < literalLength; i++, pos++) {
          if (literal.charAt(i) != seq.charAt(pos)) {
            return FAIL;
          }
        }
        return pos;
      }
      return FAIL;
    }
  }

  /**
   * Returns current position if the child matcher matches, and zero if
   * the matcher fails.
   */
  static class LookAhead implements Recognizer {

    private final Recognizer pattern;

    LookAhead(Recognizer pattern) {
      this.pattern = pattern;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      return (pattern.match(seq, pos, length) != FAIL) ? pos : FAIL;
    }

  }

  /**
   * One or more.
   */
  static class Plus extends Cardinality {
    Plus(Recognizer pattern) {
      super(pattern, 1, 0);
    }
  }

  /**
   * Zero or one.
   */
  static class QuestionMark extends Cardinality {
    QuestionMark(Recognizer pattern) {
      super(pattern, 0, 1);
    }
  }

  /**
   * Match the exact sequence of child matchers, all or nothing.
   */
  static class Sequence implements Recognizer {

    private final Recognizer[] patterns;

    Sequence(Recognizer[] patterns) {
      this.patterns = patterns;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      int result = 0;
      for (Recognizer pattern : patterns) {
        pos = pattern.match(seq, pos, length);
        if (pos == FAIL) {
          return FAIL;
        }
        result = pos;
      }
      return result;
    }

  }

  /**
   * Zero or more.
   */
  static class Star extends Cardinality {
    Star(Recognizer pattern) {
      super(pattern, 0, 0);
    }
  }


  /**
   * Use a method call to detect whitespace.
   */
  static class Whitespace implements Recognizer {

    private final boolean invert;

    Whitespace(boolean invert) {
      this.invert = invert;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      if (pos < length) {
        if (DefaultCharClassifier.whitespace(seq.charAt(pos))) {
          return invert ? FAIL : pos + 1;
        } else {
          return invert ? pos + 1 : FAIL;
        }
      }
      return FAIL;
    }

  }

  public interface Recognizer {

    int match(CharSequence seq, int pos, int length);

  }

}
