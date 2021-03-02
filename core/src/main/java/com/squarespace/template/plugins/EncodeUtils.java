package com.squarespace.template.plugins;

/**
 * Implements the encodeURI, encodeURIComponent and escape functions.
 *
 * Synopsis of escaping rules from ECMA-262 below. Each function escapes all characters except those on the right-hand
 * side:
 *
 * escape() - escapeSpecial encodeURI() - uriReserved | uriUnescaped | '#' encodeURIComponent() - uriUnescaped
 *
 * Where:
 *
 * escapeSpecial: uriAlpha | decimalDigit | escapeSymbols escapeSymbols: * _ + - . / uriReserved: ; / ? : @ & = + $ ,
 * uriUnescaped: uriAlpha | decimalDigit | uriMark uriAlpha: lower | upper lower: abcdefghijklmnopqrstuvwxyz upper:
 * ABCDEFGHIJKLMNOPQRSTUVWXYZ decimalDigit: 0-9 uriMark: - _ . ! ~ * ' ( )
 */
public class EncodeUtils {

  public static String encodeURI(String value) {
    return encode(value, true);
  }

  public static String encodeURIComponent(String value) {
    return encode(value, false);
  }

  /*
   * ECMA 3, 15.1.3 URI Handling Function Properties The following are implementations of the algorithms given in the
   * ECMA specification for the hidden functions 'Encode' and 'Decode'.
   */
  private static String encode(String str, boolean fullUri) {
    byte[] utf8buf = null;
    StringBuilder sb = null;

    for (int k = 0, length = str.length(); k != length; ++k) {
      char c = str.charAt(k);
      if (encodeUnescaped(c, fullUri)) {
        if (sb != null) {
          sb.append(c);
        }
      } else {
        if (sb == null) {
          sb = new StringBuilder(length + 3);
          sb.append(str);
          sb.setLength(k);
          utf8buf = new byte[6];
        }
        if (0xDC00 <= c && c <= 0xDFFF) {
          return null;
        }
        int V;
        if (c < 0xD800 || 0xDBFF < c) {
          V = c;
        } else {
          k++;
          if (k == length) {
            return null;
          }
          char C2 = str.charAt(k);
          if (!(0xDC00 <= C2 && C2 <= 0xDFFF)) {
            return null;
          }
          V = ((c - 0xD800) << 10) + (C2 - 0xDC00) + 0x10000;
        }
        int L = oneUcs4ToUtf8Char(utf8buf, V);
        for (int j = 0; j < L; j++) {
          int d = 0xff & utf8buf[j];
          sb.append('%');
          sb.append(toHexChar(d >>> 4));
          sb.append(toHexChar(d & 0xf));
        }
      }
    }
    return (sb == null) ? str : sb.toString();
  }

  /*
   * Convert one UCS-4 char and write it into a UTF-8 buffer, which must be at least 6 bytes long. Return the number of
   * UTF-8 bytes of data written.
   */
  private static int oneUcs4ToUtf8Char(byte[] utf8Buffer, int ucs4Char) {
    int utf8Length = 1;

    // JS_ASSERT(ucs4Char <= 0x7FFFFFFF);
    if ((ucs4Char & ~0x7F) == 0)
      utf8Buffer[0] = (byte)ucs4Char;
    else {
      int i;
      int a = ucs4Char >>> 11;
      utf8Length = 2;
      while (a != 0) {
        a >>>= 5;
        utf8Length++;
      }
      i = utf8Length;
      while (--i > 0) {
        utf8Buffer[i] = (byte)((ucs4Char & 0x3F) | 0x80);
        ucs4Char >>>= 6;
      }
      utf8Buffer[0] = (byte)(0x100 - (1 << (8 - utf8Length)) + ucs4Char);
    }
    return utf8Length;
  }

  private static boolean encodeUnescaped(char c, boolean fullUri) {
    if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9')) {
      return true;
    }
    switch (c) {
      case '-':
      case '_':
      case '.':
      case '!':
      case '~':
      case '*':
      case '(':
      case ')':
        return true;
      case ';':
      case '/':
      case '?':
      case ':':
      case '@':
      case '&':
      case '=':
      case '+':
      case '$':
      case ',':
      case '#':
        if (fullUri) {
          return true;
        }
        break;
    }
    return false;
  }

  private static char toHexChar(int i) {
    return (char)((i < 10) ? i + '0' : i - 10 + 'A');
  }


}