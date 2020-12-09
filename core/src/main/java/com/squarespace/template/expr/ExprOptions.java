package com.squarespace.template.expr;

/**
 * Options to configure an Expr.
 */
public class ExprOptions {

  private int maxTokens = 0;
  private int maxStringLen = 0;

  /**
   * Limit the number of tokens that an expression can contain.
   * Setting to <= 0 disables the limit.
   */
  public void maxTokens(int limit) {
    this.maxTokens = limit;
  }

  /**
   * Return the max tokens limit.
   */
  public int maxTokens() {
    return this.maxTokens;
  }

  /**
   * Limit the maximum size of a string that can be produced by
   * concatenation. Setting to <= 0 disables the limit.
   */
  public void maxStringLen(int limit) {
    this.maxStringLen = limit;
  }

  /**
   * Return the max string length limit.
   */
  public int maxStringLen() {
    return maxStringLen;
  }
}
