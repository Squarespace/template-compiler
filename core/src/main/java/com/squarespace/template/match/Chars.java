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


/**
 * Character definitions. Maps the formal name to each character.
 */
public class Chars {

  // 0x00
  public static final char NULL = '\0';

  // 0x09
  public static final char HORIZONTAL_TAB = '\t';

  // 0x0A
  public static final char LINE_FEED = '\n';

  // 0x0B
  public static final char VERTICAL_TAB = '\u000B';

  // 0x0C
  public static final char FORM_FEED = '\f';

  // 0x0D
  public static final char CARRIAGE_RETURN = '\r';

  // 0x20
  public static final char SPACE = ' ';

  // 0x21
  public static final char EXCLAMATION_MARK = '!';

  // 0x22
  public static final char QUOTATION_MARK = '"';

  // 0x23
  public static final char NUMBER_SIGN = '#';

  // 0x26
  public static final char AMPERSAND = '&';

  // 0x27
  public static final char APOSTROPHE = '\'';

  // 0x28
  public static final char LEFT_PARENTHESIS = '(';

  // 0x29
  public static final char RIGHT_PARENTHESIS = ')';

  // 0x2A
  public static final char ASTERISK = '*';

  // 0x2B
  public static final char PLUS_SIGN = '+';

  // 0x2C
  public static final char COMMA = ',';

  // 0x2D
  public static final char MINUS_SIGN = '-';

  // 0x2E
  public static final char PERIOD = '.';

  // 0x2F
  public static final char SLASH = '/';

  // 0x3A
  public static final char COLON = ':';

  // 0x3B
  public static final char SEMICOLON = ';';

  // 0x3C
  public static final char LESS_THAN_SIGN = '<';

  // 0x3D
  public static final char EQUALS_SIGN = '=';

  // 0x3E
  public static final char GREATER_THAN_SIGN = '>';

  // 0x40
  public static final char AT_SIGN = '@';

  // 0x5B
  public static final char LEFT_SQUARE_BRACKET = '[';

  // 0x5C
  public static final char BACKSLASH = '\\';

  // 0x5D
  public static final char RIGHT_SQUARE_BRACKET = ']';

  // 0x5F
  public static final char UNDERSCORE = '_';

  // 0x60
  public static final char GRAVE_ACCENT = '`';

  // 0x75
  public static final char LOWERCASE_LETTER_U = 'u';

  // 0x7B
  public static final char LEFT_CURLY_BRACKET = '{';

  // 0x7D
  public static final char RIGHT_CURLY_BRACKET = '}';

  // 0x7E
  public static final char TILDE = '~';

  // 0xA0
  public static final char NO_BREAK_SPACE = '\u00A0';

  // 0xFFFF per the Unicode standard, is "guaranteed not to be a Unicode character at all"
  // and is reserved for application internal use. We typically use it to indicate EOF
  // on a character stream.
  public static final char EOF = '\uFFFF';

  /**
   * Returns the base-10 integer value of a hexadecimal character.
   */
  public static int hexvalue(char ch) {
    if (ch >= '0' && ch <= '9') {
      return ch - '0';
    } else if (ch >= 'a' && ch <= 'f') {
      return ch - 'a' + 10;
    } else if (ch >= 'A' && ch <= 'F') {
      return ch - 'A' + 10;
    }
    return 0;
  }

  /**
   * Returns the hexadecimal character representation of an integer.
   */
  public static char hexchar(int v) {
    if (v >= 0 && v <= 9) {
      return (char)('0' + v);
    } else if (v >= 10 && v <= 15) {
      return (char)('a' + (v - 10));
    }
    return '0';
  }

}
