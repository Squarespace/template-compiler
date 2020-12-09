package com.squarespace.template.expr;

import static com.squarespace.template.expr.Conversions.asnum;
import static com.squarespace.template.expr.Conversions.asstr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Built-in function definitions.
 */
public class Functions {

  private Functions() { }

  /**
   * Maximum number.
   */
  public static Token max(List<Token> args) {
    return select((a, b) -> ((NumberToken)a).value > ((NumberToken)b).value, allnum(args));
  }

  /**
   * Minimum number.
   */
  public static Token min(List<Token> args) {
    return select((a, b) -> ((NumberToken)a).value < ((NumberToken)b).value, allnum(args));
  }

  /**
   * Absolute value of a number.
   */
  public static Token abs(List<Token> args) {
    List<Token> nk = allnum(args);
    if (nk.isEmpty()) {
      return null;
    }
    double value = ((NumberToken)nk.get(0)).value;
    return Tokens.num(Math.abs(value));
  }

  /**
   * Convert first argument to a number.
   */
  public static Token num(List<Token> args) {
    return args.isEmpty() ? null : Tokens.num(asnum(args.get(0)));
  }

  /**
   * Convert first argument to a string.
   */
  public static Token str(List<Token> args) {
    return args.isEmpty() ? null : Tokens.str(asstr(args.get(0)));
  }

  /**
   * Convert first argument to a boolean.
   */
  public static Token bool(List<Token> args) {
    return args.isEmpty() ? null : Tokens.bool(Conversions.asbool(args.get(0)));
  }

  /**
   * Filter the token array, returning only number tokens.
   */
  private static List<Token> allnum(List<Token> tk) {
    List<Token> r = new ArrayList<>();
    for (Token t : tk) {
      if (t.type == ExprTokenType.NUMBER) {
        r.add(t);
      } else {
        r.add(Tokens.num(asnum(t)));
      }
    }
    return r;
  }

  /**
   * Apply a predicate to pairs of elements and return the one that passes.
   */
  private static Token select(BiFunction<Token, Token, Boolean> predicate, List<Token> tk) {
    if (tk.isEmpty()) {
      return null;
    }
    Token a = tk.get(0);
    int len = tk.size();
    for (int i = 1; i < len; i++) {
      Token b = tk.get(i);
      a = predicate.apply(a, b) ? a : b;
    }
    return a;
  }
}
