package com.squarespace.template.expr;

import static com.squarespace.template.expr.Conversions.asnum;
import static com.squarespace.template.expr.Tokens.bool;
import static com.squarespace.template.expr.Tokens.num;

/**
 * Operations corresponding to JavaScript behavior.
 */
public class Operations {

  private Operations() { }

  /**
   * Multiplication (used in multiple places).
   */
  public static Token mul(Token a, Token b) {
    return num(asnum(a) * asnum(b));
  }

  /**
   * Return a boolean token indicating a < b.
   */
  public static Token lt(Token a, Token b) {
    return bool(cmp(a, b) == -1 ? true : false);
  }

  /**
   * Return a boolean token indicating a <= b.
   */
  public static Token lteq(Token a, Token b) {
    int r = cmp(a, b);
    return bool(r == -1 || r == 0 ? true : false);
  }

  /**
   * Return a boolean token indicating a > b.
   */
  public static Token gt(Token a, Token b) {
    return bool(cmp(a, b) == 1 ? true : false);
  }

  /**
   * Return a boolean token indicating a >= b.
   */
  public static Token gteq(Token a, Token b) {
    return bool(cmp(a, b) >= 0 ? true : false);
  }

  /**
   * Return a boolean token indicating a == b with non-strict equality.
   */
  public static Token eq(Token a, Token b) {
    return bool(cmp(a, b) == 0 ? true : false);
  }

  /**
   * Return a boolean token indicating a != b with non-strict equality.
   */
  public static Token neq(Token a, Token b) {
    return bool(cmp(a, b) != 0 ? true : false);
  }

  /**
   * Return a boolean token indicating a == b with strict equality.
   */
  public static Token seq(Token a, Token b) {
    return _seq(a, b) ? Tokens.TRUE : Tokens.FALSE;
  }

  /**
   * Return a boolean token indicating a !== b with strict equality.
   */
  public static Token sneq(Token a, Token b) {
    return !_seq(a, b) ? Tokens.TRUE : Tokens.FALSE;
  }

  /**
   * Strict equality a == b.
   */
  private static boolean _seq(Token a, Token b) {
    return a.type == b.type && cmp(a, b) == 0;
  }

  /**
   * Compare two token values using the Abstract Relational Comparison
   * algorithm. This is a non-strict comparison which converts arguments
   * as needed.
   *
   * Result is:
   *   -2  indicating "always false"
   *   -1  iff  a < b
   *   0   iff  a equals b
   *   1   iff  a > b
   *
   * See:
   *  https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Less_than
   *  https://tc39.es/ecma262/#sec-abstract-relational-comparison
   */
  private static int cmp(Token a, Token b) {
    if (a.type == ExprTokenType.STRING && b.type == ExprTokenType.STRING) {
      int cmp = ((StringToken)a).value.compareTo(((StringToken)b).value);
      return cmp == 0 ? 0 : cmp < 0 ? -1 : 1;
    }

    // All other values convert to a number.
    double na = asnum(a);
    double nb = asnum(b);
    if (Double.isNaN(na) || Double.isNaN(nb)) {
      // -2 means "always false"
      return -2;
    }
    return na < nb ? -1 : na == nb ? 0 : 1;
  }

}
