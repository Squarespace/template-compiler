package com.squarespace.template.expr;

/**
 * Token representing a unary or binary operator.
 */
public class OperatorToken extends Token {

  public final Operator value;

  public OperatorToken(Operator value) {
    super(ExprTokenType.OPERATOR);
    this.value = value;
  }

  @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof OperatorToken) {
        return value.type == ((OperatorToken)obj).value.type;
      }
      return false;
    }

  @Override
  public String toString() {
    return type.name() + "[" + value.type.name() + "]";
  }
}
