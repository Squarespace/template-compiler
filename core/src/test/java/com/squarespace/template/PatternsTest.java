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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.squarespace.compiler.match.Recognizers.Recognizer;


/**
 * Built-in pattern match tests, ensuring matched character ranges are fully checked.
 */
@Test(groups = { "unit" })
public class PatternsTest {

  @Test
  public void testFormatter() {
    isFormatter("pluralize");
    isFormatter("foo-bar");

    notFormatter(" foo");
    notFormatter(".bar");
    notFormatter("-foo");
    notFormatter("2foo");
    notFormatter("pluralize?");
  }

  @Test
  public void testKeyword() {
    isKeyword(".a");
    isKeyword(".if");
    isKeyword(".ifn");
    isKeyword(".section");
    isKeyword(".plural?");
    isKeyword(".foo-Bar?");
    isKeyword(".foo_");

    notKeyword(" .foo");
    notKeyword("foo.bar");
    notKeyword(".foo-bar!");
    notKeyword(".123foo");
  }

  @Test
  public void testVariable() {
    isVariable("a.b.c");
    isVariable("foo.bar.baz");
    isVariable("@");
    isVariable("@index");
    isVariable("@foo");
    isVariable("0");
    isVariable("1");
    isVariable("12");
    isVariable("0.name");
    isVariable("0.1.2.name");

    notVariable(" foo");
    notVariable(".foo");
    notVariable("-foo.bar");
    notVariable("12foo");
    notVariable("0 .foo");
    notVariable("0. foo");
    notVariable(".0");
    notVariable("0.");
  }

  private void isFormatter(String str) {
    assertTrue(matches(str, Patterns.FORMATTER));
  }

  private void notFormatter(String str) {
    assertFalse(matches(str, Patterns.FORMATTER));
  }

  private void isKeyword(String str) {
    assertTrue(matches(str, Patterns.RESERVED_WORD));
  }

  private void notKeyword(String str) {
    assertFalse(matches(str, Patterns.RESERVED_WORD));
  }

  private void isVariable(String str) {
    assertTrue(matches(str, Patterns.VARIABLE_REF_DOTTED));
  }

  private void notVariable(String str) {
    assertFalse(matches(str, Patterns.VARIABLE_REF_DOTTED));
  }

  private boolean matches(String str, Recognizer pattern) {
    int len = str.length();
    return pattern.match(str, 0, len) == len;
  }
}
