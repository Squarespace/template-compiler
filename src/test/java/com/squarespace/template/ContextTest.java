package com.squarespace.template;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.squarespace.template.Instructions.RootInst;


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
}
