package com.squarespace.template;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.template.Instructions.RootInst;
import com.squarespace.v6.utils.JSONUtils;


public class JsonTemplateTest extends UnitTestBase {

  private static final String ALPHAS = "abcdefghijklmnopqrstuvwxyz";
  
  @Test
  public void testPlural() throws CodeException {
    CodeBuilder builder = builder();
    builder.predicate(CorePredicates.PLURAL).text("A");
    builder.or(CorePredicates.SINGULAR).text("B");
    builder.or().text("C").end();
    
    RootInst root = builder.eof().code();
    assertEquals(repr(root), "{.plural?}A{.or singular?}B{.or}C{.end}");
    assertContext(execute("5", root), "A");
    assertContext(execute("1", root), "B");
    assertContext(execute("0", root), "C");
    assertContext(execute("-3.1415926", root), "C");
  }

  @Test
  public void testSection() throws CodeException {
    CodeBuilder builder = builder();
    builder.section("foo.bar").var("baz").end();

    RootInst root = builder.eof().code();
    String jsonData = "{\"foo\": {\"bar\": {\"baz\": 123}}}";
    assertEquals(repr(root), "{.section foo.bar}{baz}{.end}");
    assertContext(execute(jsonData, root), "123");
  }

  @Test
  public void testSectionMissing() throws CodeException {
    CodeBuilder builder = builder();
    builder.section("foo").text("A").or().text("B").end();
    RootInst root = builder.eof().code();
    assertContext(execute("{\"foo\": 123}", root), "A");
    assertContext(execute("{}", root), "B");
  }

  @Test
  public void testText() throws CodeException {
    String expected = "defjkl";
    RootInst root = builder().text(ALPHAS, 3, 6).text(ALPHAS, 9, 12).eof().code();
    assertContext(execute("{}", root), expected);
  }
  
  @Test
  public void testLiterals() throws CodeException {
    RootInst root = builder().metaLeft().space().tab().newline().metaRight().eof().code();
    assertContext(execute("{}", root), "{ \t\n}");
  }
  
  @Test
  public void testRepeat() throws CodeException {
    String expected = "Hi, Joe! Hi, Bob! ";
    RootInst root = builder().repeat("@").text("Hi, ").var("foo").text("! ").end().eof().code();
    assertContext(execute("[{\"foo\": \"Joe\"},{\"foo\": \"Bob\"}]", root), expected);
  }

// Currently unsupported.
//  @Test
//  public void testRepeatOr() throws CodeSyntaxException {
//    RootInst root = builder().repeat("foo").text("A").variable("@").or().text("B").end().getRoot();
//    assertEquals(repr(root), "{.repeat section foo}A{@}{.or}B{.end}");
//    assertContext(execute("\"foo\": [1, 2, 3]}", root), "A1A2A3");
//    assertContext(execute("{}", root), "B");
//  }

  @Test
  public void testVariable() throws CodeException {
    RootInst root = builder().var("foo.bar").eof().code();
    assertContext(execute("{\"foo\": {\"bar\": 123}}", root), "123");
  }
  
  @Test
  public void testJsonFormatter() throws CodeException {
    String input = "{\"foo\":123}";
    Context ctx = new Context(JSONUtils.decode(input));
    CoreFormatters.JSON.apply(ctx, Constants.EMPTY_ARGUMENTS);
    assertEquals(ctx.getBuffer().toString(), input);
    
    input = "\"hello world\"";
    ctx = new Context(JSONUtils.decode(input));
    CoreFormatters.JSON.apply(ctx, Constants.EMPTY_ARGUMENTS);
    assertEquals(ctx.getBuffer().toString(), input);
  }
  
  @Test
  public void testFormatterInstruction() throws CodeException {
    RootInst root = builder().formatter("foo", CoreFormatters.JSON).eof().code();
    assertContext(execute("{\"foo\": 123}", root), "123");
  }
  
}
