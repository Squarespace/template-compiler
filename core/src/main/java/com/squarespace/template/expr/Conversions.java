package com.squarespace.template.expr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.squarespace.template.Context;
import com.squarespace.template.GeneralUtils;

/**
 * Conversions between token and JSON types.
 */
public class Conversions {

  // Flags for decimal number parsing
  private static final int NF_DOT = 1;
  private static final int NF_E = 2;
  private static final int NF_EDIG = 4;
  private static final int NF_ESGN = 8;

  private Conversions() { }

  /**
   * Return the character at offset i, or zero if i >= length.
   */
  public static char ch(String s, int i) {
    return i < s.length() ? s.charAt(i) : 0;
  }

  /**
   * Parse a decimal integer or float number. This is a simple state machine that
   * scans a number according to JavaScript rules for decimals. Once it has
   * accepted a valid sequence of characters it uses parseFloat() to convert the chars
   * into the decimal number.
   *
   * Note that we do not support octal numbers, so a leading zero will be skipped.
   */
  public static int decimali(String str, int i, int len) {
    // Entering this method means we've seen at least one starting digit, so
    // our while loop should complete at least one iteration and upon exiting
    // the condition j > i will be satisfied.

    // Note: we do not look for a sign '-' or '+' here, as that is handled as a
    // separate token.

    int j = i;

    // flags tracking state
    int f = 0;

    // we are guaranteed at least one digit here
    loop: while (j < len) {
      char c = str.charAt(j);
      switch (c) {
        case '.':
          // decimal point can only occur once and cannot occur within the
          // exponent region
          if ((f & NF_DOT) != 0) {
            // repeated decimal point
            return -3;
          }

          if ((f & NF_E) != 0) {
            // error, unexpected decimal point in exponent
            return -4;
          }

          // we've seen a decimal point
          f |= NF_DOT;
          break;

        case 'e':
        case 'E':
          // exponent indicator can only occur once
          if ((f & NF_E) != 0) {
            break loop;
          }
          // we've seen an exponent indicator, now expect at least one digit
          f |= NF_E | NF_EDIG;
          break;

        case '-':
        case '+':
          // sign can only occur immediately after E, can only occur once. if we
          // see a sign and are expecting a digit, break out
          if ((f & NF_EDIG) == 0 || (f & NF_E) == 0 || (f & NF_ESGN) != 0) {
            break loop;
          }
          // we've seen a sign
          f |= NF_ESGN;
          break;

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
          // we saw the expected digit, clear the flag
          f &= ~NF_EDIG;
          break;

        default:
          // if we encounter any other character, we're done
          break loop;
      }
      j++;
    }

    // if we're here, then j > i

    // if we were expecting 1 digit and didn't see one, it's an error
    if ((f & NF_EDIG) != 0) {
      return -2; // indicate expected digit in exponent
    }
    return j;
  }

  /**
   * Scan a hexadecimal sequence and return the end position.
   */
  public static int hexi(String str, int i, int len) {
    int j = i;
    while (j < len) {
      char c = str.charAt(j);
      if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
        j++;
        continue;
      }
      break;
    }
    return j;
  }

  /**
   * Convert a token to the equivalent JSON value.
   */
  public static JsonNode asnode(Token t) {
    switch (t.type) {
      case STRING:
        return new TextNode(((StringToken)t).value);
      case NUMBER:
        return new DoubleNode(((NumberToken)t).value);
      case BOOLEAN:
        return ((BooleanToken)t).value ? BooleanNode.TRUE : BooleanNode.FALSE;
      case NULL:
      default:
        return NullNode.getInstance();
    }
  }

  /**
   * Resolve variable references to tokens, and ensure all tokens returned
   * are literal values. Returns null for non-literals or JSON values that
   * cannot be converted to a supported literal token.
   */
  public static Token asliteral(Context ctx, Token t) {
    if (t != null) {
      switch (t.type) {
        case VARIABLE: {
          JsonNode r = ctx.resolve(((VarToken)t).name);
          switch (r.getNodeType()) {
            case BOOLEAN:
              return r.asBoolean() ? Tokens.TRUE : Tokens.FALSE;
            case NUMBER:
              return new NumberToken(r.asDouble());
            case STRING:
              return new StringToken(r.asText());
            case NULL:
            case MISSING:
              return Tokens.NULL;

            default:
              break;

            // Objects, arrays not currently supported. Fall through..
          }
          break;
        }

        case BOOLEAN:
        case NUMBER:
        case STRING:
        case NULL:
          return t;

        default:
          break;
      }
    }
    return null;
  }

  /**
   * Parse a hexadecimal number same as JavaScript.
   */
  public static double hexnum(String v) {
    int len = v.length();
    int i = 0;
    double r = 0.0;
    int digit;
    boolean found = false;
    while (i < len) {
      digit = hexDigit(v.charAt(i));
      if (digit < 0) {
        break;
      }
      found = true;
      r *= 16;
      r += digit;
      i++;
    }
    return found ? r : Double.NaN;
  }

  /**
   * Return the integer value of a hexadecimal digit.
   */
  public static int hexDigit(char c) {
    if (c >= '0' && c <= '9') {
      return c - '0';
    }
    if (c >= 'a' && c <= 'f') {
      return 10 + (c - 'a');
    }
    if (c >= 'A' && c <= 'F') {
      return 10 + (c - 'A');
    }
    return -1;
  }

  /**
   * Convert a token into a number token.
   */
  public static double asnum(Token t) {
    switch (t.type) {
      case NUMBER:
        return ((NumberToken)t).value;
      case STRING: {
        // Handles parsing a complete hex or decimal number. Returns
        // NaN when any part of the input fails.
        String value = ((StringToken)t).value;
        if (GeneralUtils.isEmpty(value)) {
          return 0;
        }

        int len = value.length();
        int j;
        int i = 0;
        char c = value.charAt(0);

        boolean negative = false;

        negative = c == '-';

        // Check for a single sign character. We skip over it and let the
        // parse handle it.
        if (negative || c == '+') {
          i++;
        }

        if (i == len) {
          return Double.NaN;
        }

        switch (value.charAt(i)) {
          case '0':
            // check for a hexadecimal sequence
            c = ch(value, i + 1);
            if (c == 'x' || c == 'X') {
              // test for a valid hex sequence and find the bound, then
              // parse the number as a long including the sign
              j = hexi(value, i + 2, len);
              return j == len ? (negative ? -1 : 1) * hexnum(value.substring(i + 2)) : Double.NaN;
            }

            // fall through
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            // find bound of decimal number
            j = decimali(value, i, len);
            break;
          default:
            return Double.NaN;
        }

        // If the entire string is a valid decimal number parse it, including the
        // sign. Otherwise return a NaN.
        if (j == len) {
          return (negative ? -1 : 1) * Double.valueOf(value.substring(i));
        }
        return Double.NaN;
      }
      case BOOLEAN:
        return ((BooleanToken)t).value ? 1 : 0;
      case NULL:
        return 0;

      // objects and arrays will fall through

      default:
        return Double.NaN;
    }
  }

  /**
   * Convert a token to a 32-bit integer. This is necessary to match
   * the behavior of JavaScript's bitwise operators.
   */
  public static int asint(Token t) {
    // a double NaN will become integer 0
    return (int)asnum(t);
  }

  /**
   * Convert a token to a boolean token.
   */
  public static boolean asbool(Token t) {
    switch (t.type) {
      case BOOLEAN:
        return ((BooleanToken)t).value;
      case NUMBER:
        return ((NumberToken)t).value != 0.0;
      case STRING:
        return !((StringToken)t).value.isEmpty();
      default:
        return false;
    }
  }

  /**
   * Convert a token to a string token.
   */
  public static String asstr(Token t) {
    switch (t.type) {
      case BOOLEAN:
        return ((BooleanToken)t).value ? "true" : "false";
      case NUMBER:
        return Formats.number(((NumberToken)t).value);
      case STRING:
        return ((StringToken)t).value;
      case NULL:
        return "null";

      // objects and arrays will fall through

      default:
        return "";
    }
  }

}
