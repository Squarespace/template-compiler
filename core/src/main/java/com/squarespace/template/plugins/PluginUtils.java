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

import static com.squarespace.template.Patterns.WHITESPACE_RE;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.MULTILINE;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

import com.squarespace.cldrengine.CLDR;
import com.squarespace.cldrengine.api.CurrencyFormatOptions;
import com.squarespace.cldrengine.api.CurrencyType;
import com.squarespace.cldrengine.api.Decimal;


public class PluginUtils {

  private static final Pattern SLUG_KILLCHARS = Pattern.compile("[^a-zA-Z0-9\\s-]+");

  private static final Pattern SCRIPT_TAG = Pattern.compile("</", CASE_INSENSITIVE | MULTILINE);

  private static final CurrencyFormatOptions CLDR_DEFAULT_OPTIONS = new CurrencyFormatOptions();

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

  /**
   * Escape instances of HTML script tags.
   */
  public static String escapeScriptTags(String str) {
    return escapeScriptTags(str, true);
  }

  /**
   * Escape instances of HTML script tags with the released behavior flag.
   * When the flag is set U+2028 and U+2029 pass through raw. When clear
   * they are written as json escapes, safe for embedding in a script.
   */
  public static String escapeScriptTags(String str, boolean legacyLineSeparators) {
    str = SCRIPT_TAG.matcher(str).replaceAll("<\\\\/");
    if (!legacyLineSeparators) {
      // Fixed, escape the line separator characters.
      str = str.replace("\u2028", "\\u2028").replace("\u2029", "\\u2029");
    }
    return str;
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

  public static void escapeHtmlAttribute(String str, StringBuilder buf) {
    escapeHtmlAttribute(str, buf, true);
  }

  /**
   * Escape HTML attribute characters with the released behavior flag.
   * When the flag is set the single quote passes through raw, matching the
   * release. When clear it is written as &#39;, safe for single-quoted
   * attribute values.
   */
  public static void escapeHtmlAttribute(String str, StringBuilder buf, boolean legacySingleQuote) {
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
        case '\'':
          if (legacySingleQuote) {
            // Legacy, the single quote passes through raw.
            buf.append(ch);
          } else {
            // Fixed, escape it for single-quoted attribute values.
            buf.append("&#39;");
          }
          break;
        default:
          buf.append(ch);
      }
    }
  }

  public static String formatMoney(Decimal input, Locale locale) {
    return formatMoney(input, locale, true);
  }

  /**
   * Format money with the released behavior flag. When the flag is set the
   * value goes through a double, which loses precision on large values.
   * When clear the exact decimal value is formatted.
   */
  public static String formatMoney(Decimal input, Locale locale, boolean legacyDouble) {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
    DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
    if (legacyDouble) {
      // Legacy, the exact code the release shipped.
      double cents = Double.parseDouble(input.toString());
      cents /= 100;
      return format.format(cents);
    }
    // Fixed, it converts cents to dollars exactly.
    return format.format(new BigDecimal(input.toString()).movePointLeft(2));
  }

  public static String formatMoney(Decimal amount, String currencyCode, CLDR cldr) {
    CurrencyType currency = CurrencyType.fromString(currencyCode);
    return cldr.Numbers.formatCurrency(amount, currency, CLDR_DEFAULT_OPTIONS);
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
    value = WHITESPACE_RE.matcher(value).replaceAll("-");
    return value.toLowerCase();
  }

  public static String truncate(String value, int maxLen, String ellipses) {
    return truncate(value, maxLen, ellipses, true);
  }

  /**
   * Truncate with the released behavior flag. When the flag is set a
   * negative maxLen throws like the release. When clear it clamps to 0.
   */
  public static String truncate(String value, int maxLen, String ellipses, boolean legacyNegative) {
    if (!legacyNegative) {
      maxLen = Math.max(0, maxLen);
    }
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

