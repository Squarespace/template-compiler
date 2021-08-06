package com.squarespace.template.expr;

import static com.squarespace.template.expr.Operators.ADD;
import static com.squarespace.template.expr.Operators.ASN;
import static com.squarespace.template.expr.Operators.COMMA;
import static com.squarespace.template.expr.Operators.DIV;
import static com.squarespace.template.expr.Operators.EQ;
import static com.squarespace.template.expr.Operators.LOR;
import static com.squarespace.template.expr.Operators.LPRN;
import static com.squarespace.template.expr.Operators.MINUS;
import static com.squarespace.template.expr.Operators.MUL;
import static com.squarespace.template.expr.Operators.RPRN;
import static com.squarespace.template.expr.Operators.SEMI;
import static com.squarespace.template.expr.Operators.SEQ;
import static com.squarespace.template.expr.Operators.SUB;
import static com.squarespace.template.expr.Tokens.FALSE;
import static com.squarespace.template.expr.Tokens.NULL;
import static com.squarespace.template.expr.Tokens.TRUE;
import static com.squarespace.template.expr.Tokens.num;
import static com.squarespace.template.expr.Tokens.str;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.squarespace.template.Context;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.JsonUtils;

public class ExprTest {

  @Test
  public void testBasic() {
    String e;
    Context c;

    c = new Context(JsonUtils.decode("{}"));

    e = "";
    assertEquals(parse(e), emptyList());
    assertEquals(build(e), emptyList());
    assertEquals(reduce(e, c), null);

    // if no operators are applied, the result is the top of the RPN stack
    e = "1 2 3 4 5";
    assertEquals(parse(e), asList(num(1), num(2), num(3), num(4), num(5)));
    assertEquals(build(e), asList(asList(num(1), num(2), num(3), num(4), num(5))));
    assertEquals(reduce(e, c), new DoubleNode(5));

    e = "1+2";
    assertEquals(parse(e), asList(num(1), ADD, num(2)));
    assertEquals(build(e), asList(asList(num(1), num(2), ADD)));
    assertEquals(reduce(e, c), new DoubleNode(3));

    e = "1-2";
    assertEquals(parse(e), asList(num(1), SUB, num(2)));
    assertEquals(build(e), asList(asList(num(1), num(2), SUB)));
    assertEquals(reduce(e, c), new DoubleNode(-1));

    e = "1 + 2";
    assertEquals(parse(e), asList(num(1), ADD, num(2)));
    assertEquals(build(e), asList(asList(num(1), num(2), ADD)));
    assertEquals(reduce(e, c), new DoubleNode(3));

    e = "1 + -3";
    assertEquals(parse(e), asList(num(1), ADD, MINUS, num(3)));
    assertEquals(build(e), asList(asList(num(1), num(3), MINUS, ADD)));
    assertEquals(reduce(e, c), new DoubleNode(-2));

    e = "a.b.c * -1";
    assertEquals(parse(e), asList(varn("a.b.c"), MUL, MINUS, num(1)));
    assertEquals(build(e), asList(asList(varn("a.b.c"), num(1), MINUS, MUL)));
    c = new Context(JsonUtils.decode("{\"a\": {\"b\": {\"c\": 5 }}}"));
    assertEquals(reduce(e, c), new DoubleNode(-5));

    e = "\"foo\" == '\u2018bar\u2019'";
    assertEquals(parse(e), asList(str("foo"), EQ, str("\u2018bar\u2019")));
    assertEquals(build(e), asList(asList(str("foo"), str("\u2018bar\u2019"), EQ)));
    assertEquals(reduce(e, c), BooleanNode.FALSE);

    e = " max ( 1 , 2 ) ";
    assertEquals(parse(e), asList(call("max"), LPRN, num(1), COMMA, num(2), RPRN));
    assertEquals(build(e), asList(asList(Tokens.ARGS, num(1), num(2), call("max"))));
    assertEquals(reduce(e, c), new DoubleNode(2));

    e = "1 === 1";
    assertEquals(parse(e), asList(num(1), SEQ, num(1)));
    assertEquals(build(e), asList(asList(num(1), num(1), SEQ)));
    assertEquals(reduce(e, c), BooleanNode.TRUE);

    e = "false == true";
    assertEquals(parse(e), asList(FALSE, EQ, TRUE));
    assertEquals(build(e), asList(asList(FALSE, TRUE, EQ)));

    e = "null || 1";
    assertEquals(parse(e), asList(NULL, LOR, num(1)));
    assertEquals(build(e), asList(asList(NULL, num(1), LOR)));

    e = "(1 + (2 * (7 - (3 / 4))))";
    assertEquals(parse(e), asList(
        LPRN,
        num(1),
        ADD,
        LPRN,
        num(2),
        MUL,
        LPRN,
        num(7),
        SUB,
        LPRN,
        num(3),
        DIV,
        num(4),
        RPRN,
        RPRN,
        RPRN,
        RPRN));
    assertEquals(build(e), asList(
        asList(
            num(1), num(2), num(7), num(3), num(4), DIV, SUB, MUL, ADD)));
    assertEquals(reduce(e, c), new DoubleNode(13.5));

    e = "7+8;4";
    assertEquals(parse(e), asList(num(7), ADD, num(8), SEMI, num(4)));
    assertEquals(build(e), asList(
        asList(num(7), num(8), ADD),
        asList(num(4))));

    e = "@a = 8; @b = 4 / @a; @c = 3 * @b; @c";
    assertEquals(parse(e), asList(
        varn("@a"),
        ASN,
        num(8),
        SEMI,
        varn("@b"),
        ASN,
        num(4),
        DIV,
        varn("@a"),
        SEMI,
        varn("@c"),
        ASN,
        num(3),
        MUL,
        varn("@b"),
        SEMI,
        varn("@c")));
    assertEquals(build(e), asList(
        asList(varn("@a"), num(8), ASN),
        asList(varn("@b"), num(4), varn("@a"), DIV, ASN),
        asList(varn("@c"), num(3), varn("@b"), MUL, ASN),
        asList(varn("@c"))));
    assertEquals(reduce(e, c), new DoubleNode(1.5));
  }

  @Test
  public void testDebug() {
    assertEquals(debug("@a = 2 * 3 / max(c, d)"),
        "[[@a 2 3 <multiply> <args> c d max() <divide> <assign>]]");
    assertEquals(debug("\"foo\" !== \"bar\""),
        "[[\"foo\" \"bar\" <strict inequality>]]");
    assertEquals(debug("null == false || null != true"),
        "[[null false <equality> null true <inequality> <logical or>]]");
    assertEquals(Tokens.debugToken(null), "undefined");
  }

  @Test
  public void testLimits() {
    Expr e;
    JsonNode r;
    Context c;
    JsonNode o = JsonUtils.decode("{\"foo\": \"123456789\"}");

    ExprOptions opts = new ExprOptions();
    opts.maxStringLen(10);
    opts.maxTokens(10);

    // reduce phase string concatenation limit
    c = new Context(o);
    e = new Expr("foo + \"a\"", opts);
    e.build();
    r = e.reduce(c);
    assertEquals(r, new TextNode("123456789a"));

    c = new Context(o);
    e = new Expr("foo + \"ab\"", opts);
    e.build();
    r = e.reduce(c);
    assertEquals(r, null);
    assertTrue(c.getErrors().get(0).getMessage().contains("maximum string"));

    // parse phase token limit
    c = new Context(o);
    e = new Expr("1 + 3 + 5 + 7 + 9", opts);
    e.build();
    r = e.reduce(c);
    assertEquals(r, new DoubleNode(25));

    c = new Context(o);
    e = new Expr("1 + 3 + 5 + 7 + 9 + 11", opts);
    e.build();
    r = e.reduce(c);
    assertEquals(r, null);
    assertTrue(e.errors().get(0).contains("maximum number of allowed tokens"));
  }

  @Test
  public void testBareLinefeed() {
    Expr e;

    e = new Expr("\"foo \n bar\"");
    assertEquals(e.tokens().elems(), emptyList());
    assertTrue(e.errors().get(0).contains("bare line feed char"));

    e = new Expr("\"foo \r bar\"");
    assertEquals(e.tokens().elems(), emptyList());
    assertTrue(e.errors().get(0).contains("bare carriage return char"));
  }

  @Test
  public void testParseErrors() {
    Expr e;

    e = new Expr("6 + #");
    assertTrue(e.errors().get(0).contains("Unexpected char"));
    e.build();
    assertEquals(e.expressions(), emptyList());

    e = new Expr("m#n(1, 2");
    assertTrue(e.errors().get(0).contains("Unexpected char"));
    e.build();
    assertEquals(e.expressions(), emptyList());

    e = new Expr("foo \u0000");
    assertTrue(e.errors().get(0).contains("Unexpected control"));

    e = new Expr("foo \u0018");
    assertTrue(e.errors().get(0).contains("Unexpected control"));
  }

  @Test
  public void testReduceErrors() {
    Context c = new Context(JsonUtils.decode("{}"));

    reduce("1 - max(max(abs())) - 1", c);
    assertTrue(c.getErrors().get(0).getMessage().contains("calling function abs"));
    assertTrue(c.getErrors().get(1).getMessage().contains("unexpected token on stack"));
  }

  @Test
  public void testUnsupportedValues() {
    // objects and array types are not supported for operations
    Context c = new Context(JsonUtils.decode("{\"obj\":{\"a\":1,\"b\":\"foo\"},\"arr\":[1,2,3]}"));

    assertEquals(reduce("1 + obj", c), null);
    assertEquals(reduce("1 + arr", c), null);

    assertEquals(reduce("num(obj)", c), null);
    assertEquals(reduce("str(obj)", c), null);
    assertEquals(reduce("bool(obj)", c), null);

    assertEquals(reduce("num(arr)", c), null);
    assertEquals(reduce("str(arr)", c), null);
    assertEquals(reduce("bool(arr)", c), null);
  }

  @Test
  public void testUnsupportedOperators() {
    Context c = new Context(JsonUtils.decode("{}"));
    Expr e;

    // Reduce invalid expressions to ensure that unexpected operators are caught
    // during evaluation.

    e = new Expr("");
    e.reduceExpr(c, asList(num(1), num(2), SEMI));
    assertTrue(e.errors().get(0).contains("Unexpected operator"));

    e = new Expr("");
    e.reduceExpr(c,  asList(num(1), num(2), LPRN));
    assertTrue(e.errors().get(0).contains("Unexpected operator"));

    e = new Expr("");
    e.reduceExpr(c, asList(varn("@foo"), num(1), num(2), ADD, LPRN, ASN));
    assertTrue(e.errors().get(0).contains("Unexpected operator"));
  }

  @Test
  public void testReferences() {
    Context c = new Context(JsonUtils.decode("{\"a\": 123}"));

    assertEquals(reduce("@", c), null);
    assertEquals(reduce("@.a", c), new DoubleNode(123));
  }

  @Test
  public void testInvalidEscapes() {
    Context c = new Context(JsonUtils.decode("{}"));
    assertEquals(reduce("'\\UA0000000'", c), new TextNode(" "));
  }

  @Test
  public void testStrings() {
    Context c = new Context(JsonUtils.decode("{}"));

    assertEquals(reduce("'bar'", c), new TextNode("bar"));
    assertEquals(reduce("\"\\\"bar\\\"\"", c), new TextNode("\"bar\""));

    // incomplete escapes
    assertEquals(reduce("'\\", c), null);
    assertEquals(reduce("'\\'", c), null);

    // unterminated
    assertEquals(reduce("\"\\u000000", c), null);

    // single-character escapes
    assertEquals(reduce("\"\\a\\b\\c\\n\\t\\r\\f\"", c), new TextNode("abc\n\t\r\f"));

    // hex escapes
    assertEquals(reduce("\"\\x20\\x7d\\x22\\x27\"", c), new TextNode(" }\"'"));

    // ascii control code replacement
    assertEquals(reduce("\"\\x00\\x01\\x02\\x19\\x18\"", c), new TextNode("     "));

    // unicode escapes
    assertEquals(reduce("\"\\u2019\"", c), new TextNode("\u2019"));
    assertEquals(reduce("\"\\U0001f600\"", c), new TextNode("\uD83D\uDE00"));
    assertEquals(reduce("\"\\U0001f600\\U0001f600\"", c), new TextNode("\uD83D\uDE00\uD83D\uDE00"));

    // unicode escape out-of-range replacement
    assertEquals(reduce("\"\\u0003\\u000f\\u0019\\u0021\"", c), new TextNode("   !"));
    assertEquals(reduce("\"\\U00222222\"", c), new TextNode(" "));

    // ascii control code replacement
    assertEquals(reduce("\"\\u0000\\u0001\\u0018\\u0019\"", c), new TextNode("    "));
  }

  @Test
  public void testNumbers() {
    Context c = new Context(JsonUtils.decode("{}"));
    Expr e;

    assertEquals(reduce("0x01", c), new DoubleNode(0x01));
    assertEquals(reduce("0x012345678", c), new DoubleNode(0x012345678));
    assertEquals(reduce("0x111111111111111111111", c), new DoubleNode(1.2895208742556044e+24));
    assertEquals(reduce("0x11111111111111111", c), new DoubleNode(1.9676527011956855e+19));

    assertEquals(reduce("1e20", c), new DoubleNode(1e20));
    assertEquals(reduce("1e22", c), new DoubleNode(1e22));

    // ensure exponent / sign state is correctly managed
    assertEquals(reduce("1e20+1e20", c), new DoubleNode(2e20));
    assertEquals(reduce("1e+20+1e+20", c), new DoubleNode(2e20));
    assertEquals(reduce("1e-20+1e+20", c), new DoubleNode(1e20));
    assertEquals(reduce("1e-20+1e20", c), new DoubleNode(1e20));

    // invalid number sequences

    e = new Expr(".1");
    assertTrue(e.errors().get(0).contains("Unexpected char"));

    e = new Expr("0x");
    assertTrue(e.errors().get(0).contains("hex number"));

    e = new Expr("1..2");
    assertTrue(e.errors().get(0).contains("Duplicate decimal point"));

    e = new Expr("12e10.1");
    assertTrue(e.errors().get(0).contains("decimal point in exponent"));

    e = new Expr("0.0.0.");
    assertTrue(e.errors().get(0).contains("Duplicate decimal point in number"));

    e = new Expr("1e");
    assertTrue(e.errors().get(0).contains("exponent"));

    e = new Expr("1ee");
    assertTrue(e.errors().get(0).contains("exponent"));

    e = new Expr("1e-");
    assertTrue(e.errors().get(0).contains("exponent"));

    e = new Expr("1e--1");
    assertTrue(e.errors().get(0).contains("exponent"));
  }

  @Test
  public void testStringErrors() {
    Expr e;

    e = new Expr("'");
    assertTrue(e.errors().get(0).contains("Unterminated string"));

    e = new Expr("\"");
    assertTrue(e.errors().get(0).contains("Unterminated string"));

    // incomplete escapes
    e = new Expr("\"\\x\"");
    assertTrue(e.errors().get(0).contains("hex escape"));

    e = new Expr("\"\\x1\"");
    assertTrue(e.errors().get(0).contains("hex escape"));

    e = new Expr("\"\\x1q\"");
    assertTrue(e.errors().get(0).contains("hex escape"));

    e = new Expr("\"\\u\"");
    assertTrue(e.errors().get(0).contains("unicode escape"));

    e = new Expr("\"\\uq\"");
    assertTrue(e.errors().get(0).contains("unicode escape"));

    e = new Expr("\"\\u123\"");
    assertTrue(e.errors().get(0).contains("unicode escape"));
  }

  @Test
  public void testSequences() {
    Context c = new Context(JsonUtils.decode("{}"));

    assertEquals(reduce("1; 2; 3", c), new DoubleNode(3));
    assertEquals(reduce("1, 2, 3", c), new DoubleNode(3));
  }

  @Test
  public void testAssignment() {
    Expr e;

    Context c = new Context(JsonUtils.decode("{}"));
    c.setVar("@c", new DoubleNode(3));

    e = new Expr("@a = 6; @b = 2 * @a; @b / @c");
    e.build();
    assertEquals(c.getErrors(), emptyList());
    assertEquals(e.reduce(c), new DoubleNode(4));
    assertEquals(c.resolve(new Object[] {"@a"}), new DoubleNode(6));
    assertEquals(c.resolve(new Object[] {"@b"}), new DoubleNode(12));

    // invalid assignments
    e = new Expr("a = 1");
    assertEquals(e.tokens().elems(), asList(varn("a"), ASN, num(1)));
    e.build();
    assertEquals(e.reduce(c), null);

    e = new Expr("a.b.c = 1");
    assertEquals(e.tokens().elems(), asList(varn("a.b.c"), ASN, num(1)));
    e.build();
    assertEquals(e.reduce(c), null);

    e = new Expr("* = /");
    assertEquals(e.tokens().elems(), asList(MUL, ASN, DIV));
    e.build();
    assertEquals(e.reduce(c), null);

    e = new Expr("1 = 1");
    assertEquals(e.tokens().elems(), asList(num(1), ASN, num(1)));
    e.build();
    assertEquals(e.reduce(c), null);
  }

  @Test
  public void testUnaryPlusMinus() {
    Context c = new Context(JsonUtils.decode("{\"a\":5,\"b\":-5,\"c\":\"12\"}"));
    assertEquals(reduce("1 + -a", c), new DoubleNode(-4));
    assertEquals(reduce("1 + -b", c), new DoubleNode(6));
    assertEquals(reduce("1 + +a", c), new DoubleNode(6));
    assertEquals(reduce("1 + +b", c), new DoubleNode(-4));

    assertEquals(reduce("+a - -a + -a + +a", c), new DoubleNode(10));
    assertEquals(reduce("-c + -c", c), new DoubleNode(-24));

    // errors
    assertEquals(reduce("+ -", c), null);
    assertEquals(reduce("! ~", c), null);
  }

  @Test
  public void testUnaryBinaryLogicalNot() {
    Context c = new Context(JsonUtils.decode("{\"a\":5,\"b\":false,\"c\":\"foo\"}"));

    // binary
    assertEquals(reduce("~2", c), new DoubleNode(-3));
    assertEquals(reduce("a + ~2", c), new DoubleNode(2));

    // logical
    assertEquals(reduce("!a", c), BooleanNode.FALSE);
    assertEquals(reduce("!!a", c), BooleanNode.TRUE);
  }

  @Test
  public void testEquality() {
    Context c = new Context(JsonUtils.decode("{}"));

    assertEquals(reduce("1 == 1", c), BooleanNode.TRUE);
    assertEquals(reduce("1 == 2", c), BooleanNode.FALSE);
    assertEquals(reduce("1 == NaN", c), BooleanNode.FALSE);
    assertEquals(reduce("NaN == NaN", c), BooleanNode.FALSE);

    assertEquals(reduce("true == 1", c), BooleanNode.TRUE);
    assertEquals(reduce("false == 1", c), BooleanNode.FALSE);
    assertEquals(reduce("\"1\" == 1", c), BooleanNode.TRUE);

    assertEquals(reduce("1 != 1", c), BooleanNode.FALSE);
    assertEquals(reduce("1 != 2", c), BooleanNode.TRUE);
    assertEquals(reduce("\"1\" != 1", c), BooleanNode.FALSE);
  }

  @Test
  public void testStrictEquality() {
    Context c = new Context(JsonUtils.decode("{}"));

    assertEquals(reduce("1 === 1", c), BooleanNode.TRUE);
    assertEquals(reduce("1 === 2", c), BooleanNode.FALSE);
    assertEquals(reduce("1 === NaN", c), BooleanNode.FALSE);
    assertEquals(reduce("NaN === NaN", c), BooleanNode.FALSE);

    assertEquals(reduce("true === 1", c), BooleanNode.FALSE);
    assertEquals(reduce("false === 1", c), BooleanNode.FALSE);
    assertEquals(reduce("\"1\" === 1", c), BooleanNode.FALSE);

    assertEquals(reduce("1 !== 1", c), BooleanNode.FALSE);
    assertEquals(reduce("1 !== 2", c), BooleanNode.TRUE);
    assertEquals(reduce("\"1\" !== 1", c), BooleanNode.TRUE);
  }

  @Test
  public void testAdd() {
    Context c = new Context(JsonUtils.decode("{}"));

    assertEquals(reduce("1 + true", c), new DoubleNode(2));
    assertEquals(reduce("1 + false", c), new DoubleNode(1));
    assertEquals(reduce("1 + null", c), new DoubleNode(1));

    assertEquals(reduce("true + 2", c), new DoubleNode(3));
    assertEquals(reduce("false + 2", c), new DoubleNode(2));
    assertEquals(reduce("null + 2", c), new DoubleNode(2));
  }

  @Test
  public void testSubtract() {
    Context c = new Context(JsonUtils.decode("{\"a\": 5}"));

    assertEquals(reduce("a - 1", c), new DoubleNode(4));
    assertEquals(reduce("\"foo\" - 1", c), new DoubleNode(Double.NaN));
  }

  @Test
  public void testConcatenate() {
    Context c = new Context(JsonUtils.decode("{}"));

    assertEquals(reduce("\"foo\" + 1", c), new TextNode("foo1"));
    assertEquals(reduce("\"foo\" + null", c), new TextNode("foonull"));
    assertEquals(reduce("\"foo\" + true", c), new TextNode("footrue"));
    assertEquals(reduce("\"foo\" + false", c), new TextNode("foofalse"));
    assertEquals(reduce("\"\" + false", c), new TextNode("false"));

    assertEquals(reduce("1 + \"foo\"", c), new TextNode("1foo"));
    assertEquals(reduce("null + \"bar\"", c), new TextNode("nullbar"));
    assertEquals(reduce("true + \"bar\"", c), new TextNode("truebar"));
    assertEquals(reduce("false + \"bar\"", c), new TextNode("falsebar"));
    assertEquals(reduce("false + \"\"", c), new TextNode("false"));
  }

  @Test
  public void testMultiply() {
    Context c = new Context(JsonUtils.decode("{}"));

    assertEquals(reduce("5 * 0.5", c), new DoubleNode(2.5));
    assertEquals(reduce("5 * true", c), new DoubleNode(5));
    assertEquals(reduce("5 * false", c), new DoubleNode(0));
    assertEquals(reduce("5 * null", c), new DoubleNode(0));
    assertEquals(reduce("5 * \"2\"", c), new DoubleNode(10));
    assertEquals(reduce("5 * \"\"", c), new DoubleNode(0));

    assertEquals(reduce("0.5 * 5", c), new DoubleNode(2.5));
    assertEquals(reduce("true * 5", c), new DoubleNode(5));
    assertEquals(reduce("false * 5", c), new DoubleNode(0));
    assertEquals(reduce("null * 5", c), new DoubleNode(0));
    assertEquals(reduce("\"2\" * 5", c), new DoubleNode(10));
    assertEquals(reduce("\"\" * 5", c), new DoubleNode(0));
  }

  @Test
  public void testDivide() {
    Context c = new Context(JsonUtils.decode("{}"));

    assertEquals(reduce("5 / 2", c), new DoubleNode(2.5));
    assertEquals(reduce("\"foo\" / 2", c), new DoubleNode(Double.NaN));
  }

  @Test
  public void testModulus() {
    Context c = new Context(JsonUtils.decode("{}"));

    assertEquals(reduce("5 % -5", c), new DoubleNode(0));
    assertEquals(reduce("5 % -4", c), new DoubleNode(1));
    assertEquals(reduce("5 % -3", c), new DoubleNode(2));
    assertEquals(reduce("5 % -2", c), new DoubleNode(1));
    assertEquals(reduce("5 % -1", c), new DoubleNode(0));
    assertEquals(reduce("5 % 0", c), new DoubleNode(Double.NaN));
    assertEquals(reduce("5 % 1", c), new DoubleNode(0));
    assertEquals(reduce("5 % 2", c), new DoubleNode(1));
    assertEquals(reduce("5 % 3", c), new DoubleNode(2));
    assertEquals(reduce("5 % 4", c), new DoubleNode(1));
    assertEquals(reduce("5 % 5", c), new DoubleNode(0));

    assertEquals(reduce("Infinity % 10", c), new DoubleNode(Double.NaN));
    assertEquals(reduce("10 % Infinity", c), new DoubleNode(10));
    assertEquals(reduce("Infinity % Infinity", c), new DoubleNode(Double.NaN));

    assertEquals(reduce("5 % 3.21", c), new DoubleNode(1.79));
  }

  @Test
  public void testExponent() {
    Context c = new Context(JsonUtils.decode("{}"));

    assertEquals(reduce("2 ** 3", c), new DoubleNode(8));
    assertEquals(reduce("\"foo\" ** 3", c), new DoubleNode(Double.NaN));
  }

  @Test
  public void testNesting() {
    Context c = new Context(JsonUtils.decode("{}"));

    assertEquals(reduce("((1 + 2) * 3) ** 2", c), new DoubleNode(81));

    // errant right parens
    assertEquals(reduce("1 )", c), null);
    assertEquals(reduce("1 + ) 2", c), null);
  }

  @Test
  public void testBalancedParens() {
    Expr e;

    e = new Expr("@foo = (1 + 2");
    e.build();
    assertTrue(e.errors().get(0).contains("Mismatched"));

    e = new Expr("(1 + 2");
    e.build();
    assertTrue(e.errors().get(0).contains("Mismatched"));

    e = new Expr("1 + 2)");
    e.build();
    assertTrue(e.errors().get(0).contains("Mismatched"));

    e = new Expr("((1 + 2)");
    e.build();
    assertTrue(e.errors().get(0).contains("Mismatched"));

    e = new Expr("(1 + 2))");
    e.build();
    assertTrue(e.errors().get(0).contains("Mismatched"));
  }

  @Test
  public void testShift() {
    Context c = new Context(JsonUtils.decode("{}"));

    assertEquals(reduce("14 >> -1", c), new DoubleNode(0));
    assertEquals(reduce("14 >> 0", c), new DoubleNode(14));
    assertEquals(reduce("14 >> 1", c), new DoubleNode(7));
    assertEquals(reduce("14 >> 2", c), new DoubleNode(3));
    assertEquals(reduce("14 >> 3", c), new DoubleNode(1));
    assertEquals(reduce("14 >> 4", c), new DoubleNode(0));

    assertEquals(reduce("14 << -1", c), new DoubleNode(0));
    assertEquals(reduce("14 << 0", c), new DoubleNode(14));
    assertEquals(reduce("14 << 1", c), new DoubleNode(28));
    assertEquals(reduce("14 << 2", c), new DoubleNode(56));
    assertEquals(reduce("14 << 3", c), new DoubleNode(112));
    assertEquals(reduce("14 << 4", c), new DoubleNode(224));

    // non-numbers
    assertEquals(reduce("\"foo\" >> 4", c), new DoubleNode(0));
    assertEquals(reduce("\"foo\" << 4", c), new DoubleNode(0));
    assertEquals(reduce("true << 4", c), new DoubleNode(16));
    assertEquals(reduce("null << 4", c), new DoubleNode(0));
  }

  @Test
  public void testBitwise() {
    Context c = ctx();

    // or
    assertEquals(reduce("0x02 | 0x01", c), new DoubleNode(3));

    // and
    assertEquals(reduce("0x02 & 0x01", c), new DoubleNode(0));
    assertEquals(reduce("0x02 & 0x02", c), new DoubleNode(2));
    assertEquals(reduce("0x02 & 0x07", c), new DoubleNode(2));
    assertEquals(reduce("0x03 & 0x07", c), new DoubleNode(3));

    // xor
    assertEquals(reduce("0x02 ^ 0x01", c), new DoubleNode(3));
    assertEquals(reduce("0x02 ^ 0x02", c), new DoubleNode(0));
    assertEquals(reduce("0x02 ^ 0x03", c), new DoubleNode(1));
  }

  @Test
  public void testLogical() {
    Context c = ctx();

    // or
    assertEquals(reduce("true || true", c), BooleanNode.TRUE);
    assertEquals(reduce("true || false", c), BooleanNode.TRUE);
    assertEquals(reduce("false || false", c), BooleanNode.FALSE);
    assertEquals(reduce("\"foo\" || \"bar\"", c), BooleanNode.TRUE);
    assertEquals(reduce("0 || \"bar\"", c), BooleanNode.TRUE);
    assertEquals(reduce("0 || \"\"", c), BooleanNode.FALSE);

    // and
    assertEquals(reduce("true && true", c), BooleanNode.TRUE);
    assertEquals(reduce("true && false", c), BooleanNode.FALSE);
    assertEquals(reduce("1 && 1", c), BooleanNode.TRUE);
    assertEquals(reduce("1 && 2", c), BooleanNode.TRUE);
    assertEquals(reduce("1 && 0", c), BooleanNode.FALSE);
    assertEquals(reduce("true && \"foo\"", c), BooleanNode.TRUE);
    assertEquals(reduce("true && \"\"", c), BooleanNode.FALSE);
  }

  @Test
  public void testComparisons() {
    Context c = ctx();

    assertEquals(reduce("1 < 2", c), BooleanNode.TRUE);
    assertEquals(reduce("2 < 2", c), BooleanNode.FALSE);
    assertEquals(reduce("3 < 2", c), BooleanNode.FALSE);

    assertEquals(reduce("1 <= 0", c), BooleanNode.FALSE);
    assertEquals(reduce("1 <= 1", c), BooleanNode.TRUE);
    assertEquals(reduce("1 <= 2", c), BooleanNode.TRUE);

    assertEquals(reduce("1 > 2", c), BooleanNode.FALSE);
    assertEquals(reduce("2 > 2", c), BooleanNode.FALSE);
    assertEquals(reduce("3 > 2", c), BooleanNode.TRUE);

    assertEquals(reduce("1 >= 0", c), BooleanNode.TRUE);
    assertEquals(reduce("1 >= 1", c), BooleanNode.TRUE);
    assertEquals(reduce("1 >= 2", c), BooleanNode.FALSE);

    assertEquals(reduce("0 < \"1\"", c), BooleanNode.TRUE);
    assertEquals(reduce("0 < \"0x01\"", c), BooleanNode.TRUE);
    assertEquals(reduce("5 < \"1\"", c), BooleanNode.FALSE);
    assertEquals(reduce("5 < \"0x01\"", c), BooleanNode.FALSE);

    // booleans convert to numbers
    assertEquals(reduce("0 < true", c), BooleanNode.TRUE);
    assertEquals(reduce("0 < false", c), BooleanNode.FALSE);
    assertEquals(reduce("0 <= true", c), BooleanNode.TRUE);
    assertEquals(reduce("0 <= false", c), BooleanNode.TRUE);

    // non-numeric strings are cast to NaN
    assertEquals(reduce("0 < \"foo\"", c), BooleanNode.FALSE);
    assertEquals(reduce("0 < \"foo\"", c), BooleanNode.FALSE);

    // any portion of the string that is non-numeric fails
    assertEquals(reduce("0 < \"1foo\"", c), BooleanNode.FALSE);

    // strings are compared by unicode code points
    assertEquals(reduce("\"a\" < \"b\"", c), BooleanNode.TRUE);
    assertEquals(reduce("\"a\" < \"a\"", c), BooleanNode.FALSE);
    assertEquals(reduce("\"b\" < \"a\"", c), BooleanNode.FALSE);

    assertEquals(reduce("\"a\" <= \"b\"", c), BooleanNode.TRUE);
    assertEquals(reduce("\"a\" <= \"a\"", c), BooleanNode.TRUE);
    assertEquals(reduce("\"b\" <= \"a\"", c), BooleanNode.FALSE);
  }

  @Test
  public void testConstants() {
    Context c = ctx();

    assertEquals(reduce("PI", c), new DoubleNode(Expr.PI));
    assertEquals(reduce("E", c), new DoubleNode(Expr.E));
    assertEquals(reduce("-E", c), new DoubleNode(-Expr.E));
    assertEquals(reduce("Infinity", c), new DoubleNode(Double.POSITIVE_INFINITY));
    assertEquals(reduce("-Infinity", c), new DoubleNode(Double.NEGATIVE_INFINITY));
    assertEquals(reduce("NaN", c), new DoubleNode(Double.NaN));

    assertEquals(reduce("true", c), BooleanNode.TRUE);
    assertEquals(reduce("false", c), BooleanNode.FALSE);
    assertEquals(reduce("null", c), NullNode.getInstance());
  }

  @Test
  public void testFunctionCalls() {
    Context c = ctx();
    Expr e;

    // min() takes the minimum of all of its numeric arguments
    assertEquals(reduce("min ( )", c), null);
    assertEquals(reduce("min(\"foo\")", c), new DoubleNode(Double.NaN));
    assertEquals(reduce("min(\"foo\", 5, -5)", c), new DoubleNode(-5));
    assertEquals(reduce("min(5, -3, 10, -100, 17, 1000)", c), new DoubleNode(-100));

    // max() takes the maximum of all of its numeric arguments
    assertEquals(reduce("max(\"foo\", 5, -5)", c), new DoubleNode(5));

    // abs() operates only on the first argument that converts cleanly to a number
    assertEquals(reduce("abs()", c), null);
    assertEquals(reduce("abs(-5, -10, -20, -30)", c), new DoubleNode(5));
    assertEquals(reduce("abs(\"foo\")", c), new DoubleNode(Double.NaN));
    assertEquals(reduce("abs(\"-12\")", c), new DoubleNode(12));

    // bad functions
    e = new Expr("1 + a(1, 2)");
    assertTrue(e.errors().get(0).contains("Invalid function"));

    // a variable reference next to a parenthesis will stop
    // accepting tokens, terminating the expression early
    c = ctx("{\"a\": 123}");
    assertEquals(reduce("1 + a (2, 3, 4)", c), null);
    assertEquals(reduce("foobar ( 1, 2, 3 )", c), null);

    // missing nodes will reduce to null
    assertEquals(reduce("missing", c), NullNode.getInstance());
  }

  @Test
  public void testFunctionArgsBoundary() {
    Context c = ctx();

    assertEquals(build("max(1, 2) 123"), asList(asList(Tokens.ARGS, num(1), num(2), call("max"), num(123))));
    assertEquals(reduce("max(1, 2) 123", c), new DoubleNode(123));

    assertEquals(build("max(2) 1"), asList(asList(Tokens.ARGS, num(2), call("max"), num(1))));
    assertEquals(reduce("max(2) 1", c), new DoubleNode(1));
  }

  @Test
  public void testTypeConversions() {
    Context c = ctx("{\"a\":{\"b\":3.75,\"c\":\"3.1415\",\"flag\":true,\"nil\":null,\"s\":\"foo\"},"
        + "\"n0\":\"-0x1234\",\"n1\":\"-1.2e21\",\"n2\":\"--1.2\",\"n3\":\"--0x1234\"}");

    assertEquals(reduce("str()", c), null);
    assertEquals(reduce("str(\"\")", c), new TextNode(""));
    assertEquals(reduce("str(\"foobar\")", c), new TextNode("foobar"));
    assertEquals(reduce("str(zzz)", c), new TextNode("null"));
    assertEquals(reduce("str(a.b)", c), new TextNode("3.75"));
    assertEquals(reduce("str(true)", c), new TextNode("true"));
    assertEquals(reduce("str(false)", c), new TextNode("false"));
    assertEquals(reduce("str(null)", c), new TextNode("null"));
    assertEquals(reduce("str(a.flag)", c), new TextNode("true"));
    assertEquals(reduce("str(a.nil)", c), new TextNode("null"));
    assertEquals(reduce("str(a.s)", c), new TextNode("foo"));
    assertEquals(reduce("str(-Infinity)", c), new TextNode("-Infinity"));
    assertEquals(reduce("str(Infinity)", c), new TextNode("Infinity"));
    assertEquals(reduce("str(NaN)", c), new TextNode("NaN"));
    assertEquals(reduce("str(12e20)", c), new TextNode("1.2e+21"));

    assertEquals(reduce("num()", c), null);
    assertEquals(reduce("num(zzz)", c), new DoubleNode(0));
    assertEquals(reduce("num(a.c)", c), new DoubleNode(3.1415));
    assertEquals(reduce("num(\"0xcafe\")", c), new DoubleNode(0xcafe));
    assertEquals(reduce("num(\"0Xcafe\")", c), new DoubleNode(0xcafe));
    assertEquals(reduce("num(\"000123\")", c), new DoubleNode(123));
    assertEquals(reduce("num(\"0x\")", c), new DoubleNode(Double.NaN));
    assertEquals(reduce("num(\"0xfoo\")", c), new DoubleNode(Double.NaN));
    assertEquals(reduce("num(\"123foo\")", c), new DoubleNode(Double.NaN));
    assertEquals(reduce("num(\"-123\")", c), new DoubleNode(-123));
    assertEquals(reduce("num(\"-\")", c), new DoubleNode(Double.NaN));
    assertEquals(reduce("num(\"--123\")", c), new DoubleNode(Double.NaN));
    assertEquals(reduce("num(n0)", c), new DoubleNode(-0x1234));
    assertEquals(reduce("num(n1)", c), new DoubleNode(-1.2e21));
    assertEquals(reduce("num(n2)", c), new DoubleNode(Double.NaN));
    assertEquals(reduce("num(n3)", c), new DoubleNode(Double.NaN));

    assertEquals(reduce("bool()", c), null);
    assertEquals(reduce("bool(zzz)", c), BooleanNode.FALSE);
    assertEquals(reduce("bool(a.b)", c), BooleanNode.TRUE);
    assertEquals(reduce("bool(\"foo\")", c), BooleanNode.TRUE);

    assertEquals(reduce("str(num(bool(true)))", c), new TextNode("1"));
  }

  @Test
  public void testNumberFormatting() {
    Context c = ctx();

    // large magnitude
    assertEquals(reduce("str(1e20)", c), new TextNode("100000000000000000000"));
    assertEquals(reduce("str(1e21)", c), new TextNode("1e+21"));
    assertEquals(reduce("str(1e300)", c), new TextNode("1e+300"));

    // small magnitude
    assertEquals(reduce("str(1e-5)", c), new TextNode("0.00001"));
    assertEquals(reduce("str(1e-6)", c), new TextNode("0.000001"));
    assertEquals(reduce("str(1e-7)", c), new TextNode("1e-7"));
    assertEquals(reduce("str(1e-20)", c), new TextNode("1e-20"));
    assertEquals(reduce("str(1e-21)", c), new TextNode("1e-21"));
    assertEquals(reduce("str(1e-300)", c), new TextNode("1e-300"));

    // negative large magnitude
    assertEquals(reduce("str(-1e20)", c), new TextNode("-100000000000000000000"));
    assertEquals(reduce("str(-1e21)", c), new TextNode("-1e+21"));
    assertEquals(reduce("str(-1e300)", c), new TextNode("-1e+300"));

    // negative small magnitude
    assertEquals(reduce("str(-1e-5)", c), new TextNode("-0.00001"));
    assertEquals(reduce("str(-1e-6)", c), new TextNode("-0.000001"));
    assertEquals(reduce("str(-1e-7)", c), new TextNode("-1e-7"));
    assertEquals(reduce("str(-1e-20)", c), new TextNode("-1e-20"));
    assertEquals(reduce("str(-1e-21)", c), new TextNode("-1e-21"));
    assertEquals(reduce("str(-1e-300)", c), new TextNode("-1e-300"));
  }

  /**
   * Avoid binding to assertEquals(Iterable<?>, Iterable<?>) as it does not correctly compare JsonNodes. It sees
   * DoubleNode(1) == DoubleNode(2) because both have empty iterators.
   */
  private void assertEquals(JsonNode actual, JsonNode expected) {
    Assert.assertEquals((Object)actual, (Object)expected);
  }

  /**
   * The only comparison we do other than JsonNode is a Collection.
   */
  private void assertEquals(Collection<?> actual, Collection<?> expected) {
    Assert.assertEquals(actual, expected);
  }

  private void assertEquals(String actual, String expected) {
    Assert.assertEquals(actual, expected);
  }

  private Context ctx() {
    return ctx("{}");
  }

  private Context ctx(String c) {
    return new Context(JsonUtils.decode(c));
  }

  private CallToken call(String name) {
    return new CallToken(name);
  }

  private VarToken varn(String repr) {
    return new VarToken(GeneralUtils.splitVariable(repr));
  }

  private Collection<Token> parse(String s) {
    return new Expr(s).tokens().elems();
  }

  private List<List<Token>> build(String s) {
    Expr e = new Expr(s);
    e.build();
    return e.expressions();
  }

  private JsonNode reduce(String s, Context c) {
    Expr e = new Expr(s);
    e.build();
    return e.reduce(c);
  }

  private String debug(String s) {
    StringBuilder buf = new StringBuilder();
    Tokens.debug(build(s), buf);
    return buf.toString();
  }

}
