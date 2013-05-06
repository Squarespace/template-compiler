package com.squarespace.template;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;


public class TokenMatcherTest extends UnitTestBase {

  @Test
  public void testUsage() {
    CodeMaker mk = maker();
    TokenMatcher tm = new TokenMatcher(".abc? d e f");
    assertTrue(tm.keyword());
    assertEquals(tm.consume(), mk.view(".abc?"));
    assertTrue(tm.arguments());
    assertEquals(tm.consume(), mk.view(" d e f"));
    assertTrue(tm.finished());
  }
  
  @Test
  public void testMisses() {
    CodeMaker mk = maker();
    TokenMatcher tm = new TokenMatcher(" .abc?");
    assertFalse(tm.keyword());
    assertEquals(tm.remainder(), mk.view(" .abc?"));
  }
  
  
}
