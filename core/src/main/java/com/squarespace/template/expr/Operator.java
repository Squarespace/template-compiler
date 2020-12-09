package com.squarespace.template.expr;

import java.util.EnumMap;

/**
 * Operator definition with an embedded mapping of operator type to definition.
 */
public class Operator {

  /**
   * Map of operator types to definitions.
   */
  private static final EnumMap<OperatorType, Operator> OPERATORS = new EnumMap<>(OperatorType.class);

  public final OperatorType type;
  public final int prec;
  public final Assoc assoc;
  public final String desc;

  private Operator(OperatorType type, int prec, Assoc assoc, String desc) {
    this.type = type;
    this.prec = prec;
    this.assoc = assoc;
    this.desc = desc;
  }

  /**
   * Construct a new operator definition, record its type -> definition mapping, and
   * return the corresponding operator token.
   */
  public static OperatorToken op(OperatorType type, int prec, Assoc assoc, String desc) {
    Operator op = new Operator(type, prec, assoc, desc);
    OPERATORS.put(type, op);
    return new OperatorToken(op);
  }

  /**
   * Look up an operator definition by its type.
   */
  public static Operator op(OperatorType type) {
    return OPERATORS.get(type);
  }
}
