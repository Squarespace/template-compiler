package com.squarespace.template;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.template.Instructions.RootInst;
import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.v6.utils.JSONUtils;


@Test( groups={ "unit" })
public class ContextTest extends UnitTestBase {

  @Test
  public void testLookups() throws CodeException {
    CodeMaker mk = maker();
    Context ctx = context("{\"a\": {\"b\": [1,2,3]}}");
    ctx.push(mk.strarray("a"));
    assertEquals(ctx.node(), json("{\"b\": [1,2,3]}"));

    String json = "{\"a\": {\"c\": 1}, \"b\": 2}";
    RootInst root = builder().section("a").var("b").var("c").end().eof().build();
    assertContext(execute(json, root), "21");
  }
  
  @Test
  public void testLookupMiss() throws CodeException {
    CodeMaker mk = maker();
    Context ctx = context("{}");
    ctx.push(mk.strarray("a"));
    assertTrue(ctx.node().isMissingNode());
    ctx.push(mk.strarray("a", "b", "c"));
    assertTrue(ctx.node().isMissingNode());
    
    ctx = context("{\"a\": null}");
    ctx.push(mk.strarray("a", "b"));
    assertTrue(ctx.node().isTextual());
    assertTrue(ctx.node().asText().contains("Can't resolve"));
  }

  @Test
  public void testLoggingHook() throws CodeException {
    // Add a hook to ensure that all unexpected exceptions are logged.
    final List<Exception> exceptions = new ArrayList<>();
    LoggingHook hook = new LoggingHook() {
      @Override
      public void log(Exception e) {
        exceptions.add(e);
      }
    };

    CodeMaker mk = maker();
    Context ctx = context("{}");
    ctx.setSafeExecution();
    ctx.setLoggingHook(hook);

    // Execute the 'npe' formatter.
    RootInst root = builder().var("a", mk.fmt(UnitTestFormatters.NPE)).eof().build();
    ctx.execute(root);
    assertEquals(exceptions.size(), 1);
  }
  
  @Test
  public void testSafeExecutionMode() throws CodeException {
    CodeMaker mk = maker();
    Context ctx = context("{\"a\": {}}");
    ctx.setPartials(JSONUtils.decode("{\"foo\": \"this {.section x} value\"}"));
    ctx.setSafeExecution();
    ctx.setCompiler(compiler());

    // Apply a partial that contains a syntax error. In safe mode, this should just 
    // append errors to the context, not raise exceptions.
    RootInst root = builder().var("a", mk.fmt(CoreFormatters.APPLY, mk.args(" foo"))).eof().build();
    ctx.execute(root);

    assertEquals(ctx.getErrors().size(), 1);
  }
  
}
