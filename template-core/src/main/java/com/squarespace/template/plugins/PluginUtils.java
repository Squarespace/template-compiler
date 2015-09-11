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

package com.squarespace.template.plugins;

import static com.squarespace.template.Patterns.WHITESPACE;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;


public class PluginUtils {

  private static final Pattern SLUG_KILLCHARS = Pattern.compile("[^a-zA-Z0-9\\s-]+");

  private PluginUtils() {
  }

  /**
   * Translate a hex digit into its corresponding integer value, and if the character is not a valid
   * hex digit, returns -1. For example,  hexDigitToInt('e') == 14, and hexDigitToInt('x') == -1.
   *
   * @param ch  candidate hex digit character
   * @return    -1, or the integer value of the hex digit
   */
  public static int hexDigitToInt(char ch) {
    if (ch >= '0' && ch <= '9') {
      return ch - '0';
    } else if (ch >= 'a' && ch <= 'f') {
      return ch - 'a' + 10;
    } else if (ch >= 'A' && ch <= 'F') {
      return ch - 'A' + 10;
    }
    return -1;
  }

  public static void escapeHtml(String str, StringBuilder buf) {
    int length = str.length();
    for (int i = 0; i < length; i++) {
      char ch = str.charAt(i);
      switch (ch) {
        case '&':
          buf.append("&amp;");
          break;
        case '<':
          buf.append("&lt;");
          break;
        case '>':
          buf.append("&gt;");
          break;
        default:
          buf.append(ch);
      }
    }
  }

  public static void escapeHtmlTag(String str, StringBuilder buf) {
    int length = str.length();
    for (int i = 0; i < length; i++) {
      char ch = str.charAt(i);
      switch (ch) {
        case '&':
          buf.append("&amp;");
          break;
        case '<':
          buf.append("&lt;");
          break;
        case '>':
          buf.append("&gt;");
          break;
        case '"':
          buf.append("&quot;");
          break;
        default:
          buf.append(ch);
      }
    }
  }

  public static void escapeHtmlAttribute(String source, StringBuilder dest) {
    int length = source.length();
    for (int i = 0; i < length; i++) {
      char ch = source.charAt(i);
      switch (ch) {
        case '{':
          dest.append("&#123;");
          break;

        case '}':
          dest.append("&#125;");
          break;

        case '>':
          dest.append("&gt;");
          break;

        case '"':
          dest.append("&quot;");
          break;

        case '|':
          dest.append("&#124;");
          break;

        case '<':
          dest.append("&lt;");
          break;

        case '&':
          dest.append("&amp;");
          break;

        default:
          dest.append(ch);
      }
    }
  }

  public static String formatMoney(double cents, Locale locale) {
    cents /= 100;
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
    DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
    return format.format(cents);
  }

  public static String removeTags(String str) {
    StringBuilder buf = new StringBuilder();
    boolean inTag = false;
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      switch (ch) {

        case '<':
          inTag = true;
          break;

        case '>':
          inTag = false;
          buf.append(' ');
          break;

       default:
          if (!inTag) {
            buf.append(ch);
          }
      }
    }
    return buf.toString();
  }

  public static String slugify(String value) {
    value = SLUG_KILLCHARS.matcher(value).replaceAll("");
    value = WHITESPACE.matcher(value).replaceAll("-");
    return value.toLowerCase();
  }

  public static String truncate(String value, int maxLen, String ellipses) {
    if (value.length() <= maxLen) {
      return value;
    }

    int end = maxLen;
    for (int i = maxLen - 1; i >= 0; i--) {
      if (Character.isWhitespace(value.charAt(i))) {
        end = i + 1;
        break;
      }
    }
    return value.substring(0, end) + ellipses;
  }

  /**
   * Left-pads values where 0 <= n.
   */
  public static void leftPad(long value, char padChar, int maxDigits, StringBuilder buf) {
    int digits = (value == 0) ? 1 : (int) Math.log10(value) + 1;
    for (int i = 0; i < maxDigits - digits; i++) {
      buf.append(padChar);
    }
    buf.append(value);
  }

}

