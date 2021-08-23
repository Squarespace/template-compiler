package com.squarespace.template.expr;

import static com.squarespace.template.expr.OperatorType.MUL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

import org.testng.Assert;
import org.testng.annotations.Test;

public class OperatorTokenTest {

  @Test
  public void testEquality() {

    OperatorToken o1 = Operators.MUL;
    OperatorToken o2 = Operator.op(MUL, 15, Assoc.LEFT, "multiply");
    assertEquals(o1, o2);

    OperatorToken o3 = Operator.op(OperatorType.DIV, 15, Assoc.LEFT, "division");
    assertNotEquals(o1, o3);

    assertFalse(o1.equals(null));
    assertFalse(o1.equals(o3));
    assertFalse(o1.equals(MUL));
    assertFalse(o1.equals("*"));
  }
}
