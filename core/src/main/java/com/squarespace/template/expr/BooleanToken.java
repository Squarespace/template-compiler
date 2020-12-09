package com.squarespace.template.expr;

/**
 * Token representing a boolean value.
 */
public class BooleanToken extends Token {

  public final Boolean value;

  public BooleanToken(Boolean value) {
    super(ExprTokenType.BOOLEAN);
    this.value = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BooleanToken) {
      return ((BooleanToken)obj).value.equals(this.value);
    }
    return false;
  }

  @Override
  public String toString() {
    return "BooleanToken[" + (value ? "TRUE" : "FALSE") + "]";
  }
}
