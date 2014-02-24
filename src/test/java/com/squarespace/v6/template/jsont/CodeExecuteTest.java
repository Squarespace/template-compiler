package com.squarespace.template;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.DecimalNode;
import com.squarespace.template.Instructions.RootInst;
import com.squarespace.template.plugins.CorePredicates;


/**
 * Executing pieces of code and verifying output.
 */
@Test( groups={ "unit" })
public class CodeExecuteTest extends UnitTestBase {

  private static final String ALPHAS = "abcdefghijklmnopqrstuvwxyz";
  
  @Test
  public void testLiterals() throws CodeException {
    RootInst root = builder().metaLeft().space().tab().newline().metaRight().eof().build();
    assertContext(execute("{}", root), "{ \t\n}");
  }
  
  @Test
  public void testPredicates() throws CodeException {
    CodeBuilder builder = builder();
    builder.predicate(CorePredicates.PLURAL).text("A");
    builder.or(CorePredicates.SINGULAR).text("B");
    builder.or().text("C").end();
    
    Instruction root = builder.eof().build();
    assertEquals(repr(root), "{.plural?}A{.or singular?}B{.or}C{.end}");
    assertContext(execute("5", root), "A");
    assertContext(execute("1", root), "B");
    assertContext(execute("0", root), "C");
    assertContext(execute("-3.1415926", root), "C");
  }
  
  @Test
  public void testSection() throws CodeException {
    RootInst root = builder().section("foo.bar").var("baz").end().eof().build();

    String json = "{\"foo\": {\"bar\": {\"baz\": 123}}}";
    assertEquals(repr(root), "{.section foo.bar}{baz}{.end}");
    assertContext(execute(json, root), "123");
    
    root = builder().section("foo.1").var("@").end().eof().build();
    json = "{\"foo\": [\"a\", \"b\"]}";
    assertEquals(repr(root), "{.section foo.1}{@}{.end}");
    assertContext(execute(json, root), "b");
  }

  @Test
  public void testSectionMissing() throws CodeException {
    CodeBuilder builder = builder();
    builder.section("foo").text("A").or().text("B").end();
    RootInst root = builder.eof().build();
    assertContext(execute("{\"foo\": 123}", root), "A");
    assertContext(execute("{}", root), "B");
    assertContext(execute("{\"foo\": null}", root), "B");
    assertContext(execute("{\"foo\": []}", root), "B");
  }

  @Test
  public void testText() throws CodeException {
    String expected = "defjkl";
    RootInst root = builder().text(ALPHAS, 3, 6).text(ALPHAS, 9, 12).eof().build();
    assertContext(execute("{}", root), expected);
  }
  
  @Test
  public void testRepeat() throws CodeException {
    String expected = "Hi, Joe! Hi, Bob! ";
    RootInst root = builder().repeated("@").text("Hi, ").var("foo").text("! ").end().eof().build();
    assertContext(execute("[{\"foo\": \"Joe\"},{\"foo\": \"Bob\"}]", root), expected);
  }

  @Test
  public void testRepeatOr() throws CodeException {
    RootInst root = builder().repeated("foo").text("A").var("@").or().text("B").end().eof().build();
    assertEquals(repr(root), "{.repeated section foo}A{@}{.or}B{.end}");
    assertContext(execute("{\"foo\": [1, 2, 3]}", root), "A1A2A3");
    assertContext(execute("{}", root), "B");
  }

  @Test
  public void testVariable() throws CodeException {
    RootInst root = builder().var("foo.bar").eof().build();
    assertContext(execute("{\"foo\": {\"bar\": 123}}", root), "123");
    
    root = builder().var("@").eof().build();
    assertContext(execute("3.14159", root), "3.14159");
    assertContext(execute("123.000", root), "123.0");
    assertContext(execute("null", root), "");

    
    root = builder().var("foo.2.bar").eof().build();
    assertContext(execute("{\"foo\": [0, 0, {\"bar\": \"hi\"}]}", root), "hi");
    
    root = builder().var("foo").eof().build();
    assertContext(execute("{\"foo\": [\"a\", \"b\", 123]}", root), "a,b,123");
  }

  @Test
  public void testVariableTypes() throws CodeException {
    RootInst root = builder().var("@").eof().build();
    String value = "12345678900000000.1234567890000000";
    DecimalNode node = new DecimalNode(new BigDecimal(value));
    assertContext(execute(node, root), value);

    value = "123.0";
    node = new DecimalNode(new BigDecimal(value));
    assertContext(execute(node, root), value);
  }
  
}
