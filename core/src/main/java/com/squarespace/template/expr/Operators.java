package com.squarespace.template.expr;

import static com.squarespace.template.expr.Assoc.LEFT;
import static com.squarespace.template.expr.Assoc.RIGHT;
import static com.squarespace.template.expr.Operator.op;

/**
 * Operators in our expression grammar.
 */
public class Operators {

  private Operators() { }

  // unary
  public static final OperatorToken PLUS = op(OperatorType.PLUS, 17, RIGHT, "unary plus");
  public static final OperatorToken  MINUS = op(OperatorType.MINUS, 17, RIGHT, "unary minus");

  // logical not
  public static final OperatorToken LNOT = op(OperatorType.LNOT, 17, RIGHT, "logical not");

  // bitwise not
  public static final OperatorToken BNOT = op(OperatorType.BNOT, 17, RIGHT, "bitwise not");

  // math
  public static final OperatorToken POW = op(OperatorType.POW, 16, RIGHT, "exponent");
  public static final OperatorToken MUL = op(OperatorType.MUL, 15, LEFT, "multiply");
  public static final OperatorToken DIV = op(OperatorType.DIV, 15, LEFT, "divide");
  public static final OperatorToken MOD = op(OperatorType.MOD, 15, LEFT, "modulus");
  public static final OperatorToken ADD = op(OperatorType.ADD, 14, LEFT, "add");
  public static final OperatorToken SUB = op(OperatorType.SUB, 14, LEFT, "subtract");

  // arithmetic shift
  public static final OperatorToken SHL = op(OperatorType.SHL, 13, LEFT, "left shift");
  public static final OperatorToken SHR = op(OperatorType.SHR, 13, LEFT, "right shift");

  // compare
  public static final OperatorToken LT = op(OperatorType.LT, 12, LEFT, "less than");
  public static final OperatorToken GT = op(OperatorType.GT, 12, LEFT, "greater than");
  public static final OperatorToken LTEQ = op(OperatorType.LTEQ, 12, LEFT, "less than or equal");
  public static final OperatorToken GTEQ = op(OperatorType.GTEQ, 12, LEFT, "greater than or equal");

  // equality, strict and loose
  public static final OperatorToken EQ = op(OperatorType.EQ, 11, LEFT, "equality");
  public static final OperatorToken NEQ = op(OperatorType.NEQ, 11, LEFT, "inequality");
  public static final OperatorToken SEQ = op(OperatorType.SEQ, 11, LEFT, "strict equality");
  public static final OperatorToken SNEQ = op(OperatorType.SNEQ, 11, LEFT, "strict inequality");

  // bitwise operators
  public static final OperatorToken BAND = op(OperatorType.BAND, 10, LEFT, "bitwise and");
  public static final OperatorToken BXOR = op(OperatorType.BXOR, 9, LEFT, "bitwise xor");
  public static final OperatorToken BOR = op(OperatorType.BOR, 8, LEFT, "bitwise or");

  // logical operators
  public static final OperatorToken LAND = op(OperatorType.LAND, 7, LEFT, "logical and");
  public static final OperatorToken LOR = op(OperatorType.LOR, 6, LEFT, "logical or");

  // assignment
  public static final OperatorToken ASN = op(OperatorType.ASN, 3, RIGHT, "assign");

  // fake operators
  public static final OperatorToken SEMI = op(OperatorType.SEMI, 1, LEFT, "semicolon");
  public static final OperatorToken COMMA = op(OperatorType.COMMA, 1, RIGHT, "comma");
  public static final OperatorToken LPRN = op(OperatorType.LPRN, 1, LEFT, "left parenthesis");
  public static final OperatorToken RPRN = op(OperatorType.RPRN, 1, LEFT, "right parenthesis");

}
