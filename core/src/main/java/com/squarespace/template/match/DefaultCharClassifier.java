/**
 * Copyright (c) 2017 Squarespace, Inc.
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


public class DefaultCharClassifier implements CharClassifier {

  public static final int NONE = 0;

  public static final int DIGIT = 1 << 0;

  public static final int LOWERCASE = 1 << 1;

  public static final int UPPERCASE = 1 << 2;

  public static final int NONASCII = 1 << 3;

  public static final int NONPRINTABLE = 1 << 4;

  public static final int HEXDIGIT = 1 << 5;

  public static final int NUMBER_SIGN = 1 << 6;

  public static final int URI_RESERVED = 1 << 7;

  public static final int URI_MARK = 1 << 8;

  public static final int ESCAPE_SYMS = 1 << 9;

  public static final int DASH = 1 << 10;

  public static final int UNDERSCORE = 1 << 11;

  private static final int LIMIT = 0x80;

  // Mapping of character values to their class membership flags.
  private static final int[] CHARACTER_CLASSES = new int[LIMIT];

  static {
    for (int i = 0; i < LIMIT; i++) {
      CHARACTER_CLASSES[i] = classify((char)i);
    }
  }

  /**
   * Tests whether a character is a member of the given classes.
   */
  public boolean isMember(char ch, int cls) {
    return ch < LIMIT ? (CHARACTER_CLASSES[ch] & cls) > 0 : false;
  }

  /**
   * Builds a string containing all characters that are members of the
   * given classes, in ascending order.
   */
  public String membersOf(int cls) {
    StringBuilder buf = new StringBuilder();
    for (char ch = '\u0000'; ch < LIMIT; ch++) {
      if ((CHARACTER_CLASSES[ch] & cls) > 0) {
        buf.append(ch);
      }
    }
    return buf.toString();
  }

  /**
   * V8 JavaScript engine's whitespace ranges.
   */
  public static boolean whitespace(char ch) {
    return (ch == ' ')
        || (ch >= '\t' && ch <= '\r')
        || (ch == '\u00a0')   // nbsp
        || (ch == '\u1680')   // ogham space
        || (ch == '\u180e')   // mongolian vowel separator
        || (ch >= '\u2000' && ch <= '\u200a')   // [en space, ..., hair space]
        || (ch == '\u2028')   // line separator
        || (ch == '\u2029')   // paragraph separator
        || (ch == '\u202f')   // narrow nbsp
        || (ch == '\u205f')   // medium mathematical space
        || (ch == '\u3000')   // ideographic space
        || (ch == '\ufeff');  // byte order mark
  }

  private static int classify(char ch) {
    switch (ch) {
      case '\u0000':
      case '\u0001':
      case '\u0002':
      case '\u0003':
      case '\u0004':
      case '\u0005':
      case '\u0006':
      case '\u0007':
      case '\u0008':
        return NONPRINTABLE;

      case '\u000E':
      case '\u000F':
      case '\u0010':
      case '\u0011':
      case '\u0012':
      case '\u0013':
      case '\u0014':
      case '\u0015':
      case '\u0016':
      case '\u0017':
      case '\u0018':
      case '\u0019':
      case '\u001A':
      case '\u001B':
      case '\u001C':
      case '\u001D':
      case '\u001E':
      case '\u001F':
        return NONPRINTABLE;

      case '!':
        return URI_MARK;

      case '#':
        return NUMBER_SIGN;

      case '$':
        return URI_RESERVED;

      case '%':
        return NONE;

      case '&':
        return URI_RESERVED;

      case '\'':
      case '(':
      case ')':
        return URI_MARK;

      case '*':
        return ESCAPE_SYMS | URI_MARK;

      case '+':
        return ESCAPE_SYMS | URI_RESERVED;

      case ',':
        return URI_RESERVED;

      case '-':
        return DASH | ESCAPE_SYMS | URI_MARK;

      case '.':
        return ESCAPE_SYMS | URI_MARK;

      case '/':
        return ESCAPE_SYMS | URI_RESERVED;

      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        return DIGIT | HEXDIGIT;

      case ':':
      case ';':
      case '=':
        return URI_RESERVED;

      case '>':
        return NONE;

      case '?':
        return URI_RESERVED;

      case '@':
        return ESCAPE_SYMS | URI_RESERVED;

      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F':
        return UPPERCASE | HEXDIGIT;

      case 'G':
      case 'H':
      case 'I':
      case 'J':
      case 'K':
      case 'L':
      case 'M':
      case 'N':
      case 'O':
      case 'P':
      case 'Q':
      case 'R':
      case 'S':
      case 'T':
      case 'U':
      case 'V':
      case 'W':
      case 'X':
      case 'Y':
      case 'Z':
        return UPPERCASE;

      case '_':
        return UNDERSCORE | ESCAPE_SYMS | URI_MARK;

      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
        return LOWERCASE | HEXDIGIT;

      case 'g':
      case 'h':
      case 'i':
      case 'j':
      case 'k':
      case 'l':
      case 'm':
      case 'n':
      case 'o':
      case 'p':
      case 'q':
      case 'r':
      case 's':
      case 't':
      case 'u':
      case 'v':
      case 'w':
      case 'x':
      case 'y':
      case 'z':
        return LOWERCASE;

      case '{':
      case '|':
      case '}':
        return NONE;

      case '~':
        return URI_MARK;

      default:
        break;
    }

    return ch < Chars.NO_BREAK_SPACE ? NONE : NONASCII;
  }

}
