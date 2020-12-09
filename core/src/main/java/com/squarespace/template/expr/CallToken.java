package com.squarespace.template.expr;

import java.util.Objects;

/**
 * Token representing a function call.
 */
public class CallToken extends Token {

  public final String name;

  public CallToken(String name) {
    super(ExprTokenType.CALL);
    this.name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CallToken) {
      return Objects.equals(name, ((CallToken)obj).name);
    }
    return false;
  }

  @Override
  public String toString() {
    return "CallToken[" + name + "]";
  }
}
