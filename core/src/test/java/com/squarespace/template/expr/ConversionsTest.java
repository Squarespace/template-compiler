package com.squarespace.template.expr;

import static com.squarespace.template.expr.Conversions.asnum;
import static com.squarespace.template.expr.Conversions.hexnum;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class ConversionsTest {

  @Test
  public void testHexnum() {
    assertEquals(hexnum(""), Double.NaN);
    assertEquals(hexnum("0"), 0.0);
    assertEquals(hexnum("abcd"), 43981.0);
    assertEquals(hexnum("111111111111111111111"), 1.2895208742556044e+24);
  }

  @Test
  public void testAsnum() {
    assertEquals(asnum(str("")), 0.0);
    assertEquals(asnum(str("-")), Double.NaN);
    assertEquals(asnum(str("+")), Double.NaN);
    assertEquals(asnum(str("z")), Double.NaN);
    assertEquals(asnum(str("-z")), Double.NaN);
    assertEquals(asnum(str("123")), 123.0);
    assertEquals(asnum(str("+123")), 123.0);
    assertEquals(asnum(str("012")), 12.0);
    assertEquals(asnum(str("-0x")), Double.NaN);
    assertEquals(asnum(str("-0X")), Double.NaN);
    assertEquals(asnum(str("0xbeef")), 48879.0);
    assertEquals(asnum(str("0XBEEF")), 48879.0);
    assertEquals(asnum(str("0x1zzz")), Double.NaN);
    assertEquals(asnum(str("123zzz")), Double.NaN);
    assertEquals(asnum(str("-111111111111111111111")), -1.1111111111111111E20);
    assertEquals(asnum(str("-0x111111111111111111111")), -1.2895208742556044e+24);

    assertEquals(asnum(str("0x" + repeat("aaaaaaaaaa", 100))), Double.POSITIVE_INFINITY);
    assertEquals(asnum(str("-0x" + repeat("aaaaaaaaaa", 100))), Double.NEGATIVE_INFINITY);
    assertEquals(asnum(str(repeat("9999999999", 100))), Double.POSITIVE_INFINITY);
    assertEquals(asnum(str("-" + repeat("9999999999", 100))), Double.NEGATIVE_INFINITY);
  }

  private static Token str(String v) {
    return new StringToken(v);
  }

  private static String repeat(String v, int times) {
    StringBuilder r = new StringBuilder();
    for (int i = 0; i < times; i++) {
      r.append(v);
    }
    return r.toString();
  }
}
