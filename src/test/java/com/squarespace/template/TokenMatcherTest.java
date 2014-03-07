/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.template;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.squarespace.template.CodeMaker;
import com.squarespace.template.TokenMatcher;


@Test( groups={ "unit" })
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
  
  @Test
  public void testRange() {
    TokenMatcher tm = new TokenMatcher(".abc?");
    assertTrue(tm.keyword());
    assertEquals(tm.matchStart(), 0);
    assertEquals(tm.matchEnd(), 5);
  }
  
}
