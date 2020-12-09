package com.squarespace.template.expr;

/**
 * Marker token indicating the end of the arguments list for a function call.
 */
public class ArgsToken extends Token {

  public ArgsToken() {
    super(ExprTokenType.ARGS);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ArgsToken;
  }

  @Override
  public String toString() {
    return "ArgsToken";
  }
}
