package com.squarespace.template;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.template.Instructions.RootInst;


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
  
}
