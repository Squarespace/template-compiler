package com.squarespace.template.expr;

import java.util.Arrays;

/**
 * Token representing a variable name. Could hold a reference or
 * a definition.
 */
public class VarToken extends Token {

  public final Object[] name;

  public VarToken(Object[] name) {
    super(ExprTokenType.VARIABLE);
    this.name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof VarToken) {
      return Arrays.equals(name, ((VarToken)obj).name);
    }
    return false;
  }

  @Override
  public String toString() {
    return "VarToken[" + Arrays.toString(name) + "]";
  }
}
