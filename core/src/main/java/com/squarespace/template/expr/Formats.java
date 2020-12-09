package com.squarespace.template.expr;

import java.text.DecimalFormat;

/**
 * Miscellaneous formatting and parsing functions.
 */
public class Formats {

  // Format for fully-expanded decimal representation with up to 20 decimal digits.
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.####################");

  // Format for an exponential representation with up to 20 decimal digits.
  private static final DecimalFormat EXP_FORMAT = new DecimalFormat("0.####################E0");

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

  /**
   * Format a number with the same output as JavaScript.
   */
  public static String number(double n) {
    if (Double.isNaN(n)) {
      return "NaN";
    }
    if (Double.isInfinite(n)) {
      return n > 0 ? "Infinity" : "-Infinity";
    }

    // If the number is too large or too small to represent with all digits
    // expanded, display an exponential format.
    if ((n >= 1e21) || (n > 0 && n <= 1e-21) || (n < 0 && n >= -1e-21) || (n <= -1e21)) {
      return fixExponent(EXP_FORMAT.format(n));
    }
    // Display expanded decimal digits
    return DECIMAL_FORMAT.format(n);
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

  /**
   * Correct Java's exponential format to match JavaScript's output.
   * This is not a general method, it is only called by the number()
   * formatting method which ensures the string represents a finite
   * number that was formatted using EXP_FORMAT above.
   */
  private static String fixExponent(String s) {
    int i = s.indexOf('E');
    if (i == -1) {
      return s;
    }

    String base = s.substring(0, i);
    String exp = s.substring(i);

    // Trim trailing zeros from base
    int len = base.length();
    int k = len;
    for (int j = len - 1; j >= 0; j--) {
      char c = base.charAt(j);
      if (c != '0') {
        break;
      }
      k = j;
    }

    // Assemble the final format
    StringBuilder buf = new StringBuilder();
    buf.append(base.substring(0, k));
    buf.append('e');
    boolean negative = exp.charAt(1) == '-';
    buf.append(negative ? '-' : '+');
    buf.append(exp.substring(negative ? 2 : 1));
    return buf.toString();
  }
}
