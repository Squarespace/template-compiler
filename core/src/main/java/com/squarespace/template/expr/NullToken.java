package com.squarespace.template.expr;

/**
 * Token representing a NULL value.
 */
public class NullToken extends Token {

  public NullToken() {
    super(ExprTokenType.NULL);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof NullToken;
  }

  @Override
  public String toString() {
    return "NullToken";
  }
}
