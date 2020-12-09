package com.squarespace.template.expr;

import java.util.List;

/**
 * Interface implemented by expression functions.
 */
@FunctionalInterface
public interface FunctionDef {

  /**
   * Apply the function to the arguments and return a result. Returns
   * null if the function was unable to apply or otherwise encountered
   * an error.
   */
  Token apply(List<Token> args);

}