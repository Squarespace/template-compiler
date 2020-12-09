package com.squarespace.template.expr;

import java.util.Objects;

/**
 * Token representing a numeric value.
 */
public class NumberToken extends Token {

  public final Double value;

  public NumberToken(Double value) {
    super(ExprTokenType.NUMBER);
    this.value = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NumberToken) {
      return Objects.equals(this.value, ((NumberToken)obj).value);
    }
    return false;
  }

  @Override
    public String toString() {
      return "NUMBER[" + value + "]";
    }

}
