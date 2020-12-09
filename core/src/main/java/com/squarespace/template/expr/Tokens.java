package com.squarespace.template.expr;

import java.util.List;

import com.fasterxml.jackson.databind.node.TextNode;
import com.squarespace.template.ReprEmitter;

/**
 * Constant tokens and token builder methods.
 */
public class Tokens {

  private Tokens() { }

  public static final Token MINUS_ONE = new NumberToken(-1d);
  public static final Token PI = new NumberToken(3.141592653589793);
  public static final Token E = new NumberToken(2.718281828459045);
  public static final Token INFINITY = new NumberToken(Double.POSITIVE_INFINITY);
  public static final Token NAN = new NumberToken(Double.NaN);
  public static final Token TRUE = new BooleanToken(true);
  public static final Token FALSE = new BooleanToken(false);
  public static final Token NULL = new NullToken();
  public static final Token ARGS = new ArgsToken();

  /**
   * Build a new NumberToken.
   */
  public static Token num(double value) {
    return new NumberToken(value);
  }

  /**
   * Build a new StringToken.
   */
  public static Token str(String value) {
    return new StringToken(value);
  }

  /**
   * Return a BooleanToken.
   */
  public static Token bool(boolean value) {
    return value ? TRUE : FALSE;
  }

  public static void debug(List<List<Token>> expressions, StringBuilder buf) {
    buf.append("[");
    // Emit the assembled expressions
    for (int i = 0; i < expressions.size(); i++) {
      if (i > 0) {
        buf.append(", ");
      }
      List<Token> e = expressions.get(i);
      buf.append('[');
      for (int j = 0; j < e.size(); j++) {
        if (j > 0) {
          buf.append(' ');
        }
        buf.append(Tokens.debugToken(e.get(j)));
      }
      buf.append(']');
    }
    buf.append(']');
  }

  public static String debugToken(Token t) {
    if (t == null) {
      return "undefined";
    }
    switch (t.type) {
      case BOOLEAN:
        return ((BooleanToken)t).value ? "true" : "false";
      case NULL:
        return "null";
      case NUMBER:
        return Formats.number(((NumberToken)t).value);
      case STRING:
        return new TextNode(((StringToken)t).value).toString();
      case OPERATOR:
        return "<" + ((OperatorToken)t).value.desc + ">";
      case VARIABLE: {
        StringBuilder buf = new StringBuilder();
        ReprEmitter.emitNames(((VarToken)t).name, buf);
        return buf.toString();
      }
      case CALL:
        return ((CallToken)t).name + "()";
      case ARGS:
        return "<args>";
      default:
        return "<unk>";
    }
  }
}
