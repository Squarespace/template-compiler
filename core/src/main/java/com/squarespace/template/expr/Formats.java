package com.squarespace.template.expr;

import com.squarespace.template.dtoa.DToA;
import com.squarespace.template.v8dtoa.FastDtoa;

/**
 * Miscellaneous formatting and parsing functions.
 */
public class Formats {

  private Formats() { }

  /**
   * Parse a hexadecimal number. We use this since Long.parseLong()
   * throws an error for very large values. We instead accumulate
   * the number in a double which will eventually overflow to Infinity.
   * This matches the behavior of JavaScript's parseInt(n, 16).
   */
  public static double parseHex(String s) {
    boolean valid = false;
    int len = s.length();
    int i = 0;

    // Accumulate the number incrementally by multiplying by the radix
    // and adding each digit.
    double r = 0.0;
    while (i < len) {
      char c = s.charAt(i);
      int digit = digit(c, true);

      // If we hit an invalid digit, bail out
      if (digit < 0) {
        break;
      }
      valid = true;
      r *= 16;
      r += digit;
      i++;
    }
    return valid ? r : Double.NaN;
  }

  public static String number(double d) {
    if (Double.isNaN(d))
        return "NaN";
    if (d == Double.POSITIVE_INFINITY)
        return "Infinity";
    if (d == Double.NEGATIVE_INFINITY)
        return "-Infinity";
    if (d == 0.0)
        return "0";

    // V8 FastDtoa can't convert all numbers, so try it first but
    // fall back to old DToA in case it fails
    String result = FastDtoa.numberToString(d);
    if (result != null) {
        return result;
    }
    StringBuilder buffer = new StringBuilder();
    DToA.JS_dtostr(buffer, DToA.DTOSTR_STANDARD, 0, d);
    return buffer.toString();
  }

  /**
   * Return the integer represented by a decimal or hexadecimal digit.
   * Returns -1 if character is out of range.
   */
  private static int digit(char c, boolean hex) {
    int n = -1;
    if (c >= '0' && c <= '9') {
      n = c - '0';
    } else if (hex) {
      if (c >= 'a' && c <= 'f') {
        n = c - 'a' + 10;
      } else if (c >= 'A' && c <= 'F') {
        n = c - 'A' + 10;
      }
    }
    return n < 16 ? n : -1;
  }

}
