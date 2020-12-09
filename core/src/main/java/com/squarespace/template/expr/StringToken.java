package com.squarespace.template.expr;

import java.util.Objects;

import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Token representing a string value.
 */
public class StringToken extends Token {

  public final String value;

  public StringToken(String value) {
    super(ExprTokenType.STRING);
    this.value = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof StringToken) {
      return Objects.equals(value, ((StringToken)obj).value);
    }
    return false;
  }

  @Override
  public String toString() {
    return "StringToken[" + new TextNode(this.value) + "]";
  }
}
