package com.squarespace.template.expr;

/**
 * Base class for all tokens in our expression grammar.
 */
public abstract class Token {

  public final ExprTokenType type;

  protected Token(ExprTokenType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return type.name();
  }
}
