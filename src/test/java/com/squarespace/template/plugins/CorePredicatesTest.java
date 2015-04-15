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

package com.squarespace.template.plugins;

import static com.squarespace.template.Constants.EMPTY_ARGUMENTS;
import static com.squarespace.template.plugins.CorePredicates.EQUAL;
import static com.squarespace.template.plugins.CorePredicates.EVEN;
import static com.squarespace.template.plugins.CorePredicates.GREATER_THAN;
import static com.squarespace.template.plugins.CorePredicates.LESS_THAN;
import static com.squarespace.template.plugins.CorePredicates.LESS_THAN_OR_EQUAL;
import static com.squarespace.template.plugins.CorePredicates.NTH;
import static com.squarespace.template.plugins.CorePredicates.ODD;
import static org.testng.Assert.assertEquals;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;
import com.squarespace.template.Predicate;
import com.squarespace.template.UnitTestBase;


public class CorePredicatesTest extends UnitTestBase {

  @Test
  public void testDebug() throws CodeException {
    String[] key = new String[] { "a", "b" };
    Context ctx = context("{\"debug\": true, \"a\": {\"b\": 1}}");
    ctx.pushSection(key);
    assertTrue(CorePredicates.DEBUG, ctx);

    for (String json : new String[] {
        "{\"a\": {\"b\": 1}}",
        "{\"debug\": 0, \"a\": {\"b\": 1}}" }) {
      ctx = context(json);
      ctx.pushSection(key);
      assertFalse(CorePredicates.DEBUG, ctx);  }
    }

  @Test
  public void testEqual() throws CodeException {
    CodeMaker mk = maker();
    assertTrue(EQUAL, context("3"), mk.args(" 3"));
    assertTrue(EQUAL, context("\"hello\""), mk.args(" \"hello\""));
    assertTrue(EQUAL, context("[1, 2, 3]"), mk.args(":[1, 2, 3]"));
    assertTrue(EQUAL, context("{\"foo\": \"bar\"}"), mk.args(" {\"foo\":\"bar\"}"));

    assertFalse(EQUAL, context("-1"), mk.args(" 3"));
    assertFalse(EQUAL, context("\"hello\""), mk.args(" \"goodbye\""));
    assertFalse(EQUAL, context("[1, 2, 3]"), mk.args(":1"));
    assertFalse(EQUAL, context("{\"foo\":\"bar\"}"), mk.args(":1"));

    // Compare 2 variables
    String template = "{.equal? a b}yes{.or}no{.end}";
    Context ctx = execute(template, "{\"a\": 1, \"b\": 1}");
    assertEquals(ctx.buffer().toString(), "yes");

    ctx = execute(template, "{\"a\": 1, \"b\": 2}");
    assertEquals(ctx.buffer().toString(), "no");

    // Compare variable with JSON
    template = "{.equal? 1 b}yes{.or}no{.end}";
    ctx = execute(template, "{\"b\": 1}");
    assertEquals(ctx.buffer().toString(), "yes");

    ctx = execute(template, "{\"b\": 2}");
    assertEquals(ctx.buffer().toString(), "no");
  }

  @Test
  public void testEven() throws CodeException {
    assertTrue(EVEN, context("4"));
    assertTrue(EVEN, context("-4"));
    assertFalse(EVEN, context("5"));
    assertFalse(EVEN, context("-5"));

    assertFalse(EVEN, context("false"));
    assertFalse(EVEN, context("\"hi\""));
    assertFalse(EVEN, context("\"4\""));
    assertFalse(EVEN, context("{\"foo\": \"bar\"}"));

    // Verify behavior inside repeated section
    String template = "{.repeated section items}{.even?}{@index}{.end}{.end}";
    JsonNode json = json("{\"items\": [{}, {}, {}, {}, {}]}");
    Context ctx = execute(template, json);
    assertEquals(ctx.buffer().toString(), "");

    template = "{.repeated section items}{.even? @index}{@index}{.end}{.end}";
    ctx = execute(template, json);
    assertEquals(ctx.buffer().toString(), "24");
  }

  @Test
  public void testGreaterThan() throws CodeException {
    CodeMaker mk = maker();
    assertTrue(GREATER_THAN, context("3"), mk.args(" 1"));
    assertTrue(GREATER_THAN, context("-17"), mk.args(" -18"));
    assertTrue(GREATER_THAN, context("\"z\""), mk.args(" \"a\""));

    assertFalse(GREATER_THAN, context("1"), mk.args(" 1"));
    assertFalse(GREATER_THAN, context("-18"), mk.args(" -17"));
    assertFalse(GREATER_THAN, context("\"a\""), mk.args(" \"z\""));
    assertFalse(GREATER_THAN, context("[]"), mk.args(" []"));
  }

  @Test
  public void testJsonPredicateVariable() throws CodeException {
    Context ctx = context("{\"number\": 1, \"foo\": {\"bar\": 1}}");
    ctx.pushSection(new String[] { "foo", "bar" });
    assertTrue(EQUAL, ctx, maker().args(" number"));

    String template = "{.repeated section nums}{.equal? @index 2}{@}{.end}{.end}";
    JsonNode json = json("{\"nums\": [10, 20, 30]}");
    ctx = execute(template, json);
    assertEquals(ctx.buffer().toString(), "20");
  }

  @Test
  public void testLessThan() throws CodeException {
    CodeMaker mk = maker();
    assertTrue(LESS_THAN, context("1"), mk.args(" 2"));
    assertTrue(LESS_THAN, context("-1"), mk.args(" 0"));
    assertTrue(LESS_THAN, context("\"a\""), mk.args(" \"j\""));

    assertFalse(LESS_THAN, context("-17"), mk.args(" -18"));
    assertFalse(LESS_THAN, context("-17"), mk.args(" -18"));
    assertFalse(LESS_THAN, context("\"j\""), mk.args(" \"a\""));
    assertFalse(LESS_THAN, context("[]"), mk.args(" []"));
  }

  @Test
  public void testLessThanOrEqual() throws CodeException {
    CodeMaker mk = maker();
    assertTrue(LESS_THAN_OR_EQUAL, context("1"), mk.args(" 1"));
    assertTrue(LESS_THAN_OR_EQUAL, context("1"), mk.args(" 2"));
    assertTrue(LESS_THAN_OR_EQUAL, context("3.1415"), mk.args(" 3.2"));
    assertTrue(LESS_THAN_OR_EQUAL, context("3.1415"), mk.args(" 3.1415"));
    assertTrue(LESS_THAN_OR_EQUAL, context("\"c\""), mk.args(" \"c\""));
    assertTrue(LESS_THAN_OR_EQUAL, context("[]"), mk.args(" []"));

    assertFalse(LESS_THAN_OR_EQUAL, context("-10"), mk.args(" -20"));
    assertFalse(LESS_THAN_OR_EQUAL, context("-10"), mk.args(" -20"));
    assertFalse(LESS_THAN_OR_EQUAL, context("3.1415"), mk.args(" 3.1"));
    assertFalse(LESS_THAN_OR_EQUAL, context("\"z\""), mk.args(" \"j\""));
  }

  @Test
  public void testNth() throws CodeException {
    CodeMaker mk = maker();
    assertTrue(NTH, context("3"), mk.args(" 3"));
    assertFalse(NTH, context("2"), mk.args(" 3"));
    assertFalse(NTH, context("3"), mk.args(" 2"));
    assertFalse(NTH, context("[]"), mk.args(" 2"));

    assertFalse(NTH, context("\"a\""), mk.args(" 2"));
    assertFalse(NTH, context("2"), mk.args(" \"a\""));

    String template = "{.nth? num 3}A{.or}B{.end}";
    assertEquals(execute(template, "{\"num\": 6}").buffer().toString(), "A");
    assertEquals(execute(template, "{\"num\": 5}").buffer().toString(), "B");
    assertEquals(execute(template, "{}").buffer().toString(), "B");

    template = "{.repeated section @}{.nth? @index 3}A{.end}{.end}";
    assertEquals(execute(template, "[0,0,0]").buffer().toString(), "A");
    assertEquals(execute(template, "[0,0,0,0,0,0]").buffer().toString(), "AA");
  }

  @Test
  public void testOdd() throws CodeException {
    assertTrue(ODD, context("3"));
    assertFalse(ODD, context("4"));

    // Verify behavior inside repeated section
    String template = "{.repeated section items}{.odd?}{@index}{.end}{.end}";
    JsonNode json = json("{\"items\": [{}, {}, {}, {}, {}]}");
    Context ctx = execute(template, json);
    assertEquals(ctx.buffer().toString(), "");

    template = "{.repeated section items}{.odd? @index}{@index}{.end}{.end}";
    ctx = execute(template, json);
    assertEquals(ctx.buffer().toString(), "135");

  }

  @Test
  public void testPlural() throws CodeException {
    assertFalse(CorePredicates.PLURAL, context("0"));
    assertFalse(CorePredicates.PLURAL, context("0.10"));
    assertFalse(CorePredicates.PLURAL, context("1"));
    assertFalse(CorePredicates.PLURAL, context("1.1"));
    assertFalse(CorePredicates.PLURAL, context("1.99"));
    // Integer part must be 2 or greater
    assertTrue(CorePredicates.PLURAL, context("2"));
    assertTrue(CorePredicates.PLURAL, context("3.14159"));
    assertTrue(CorePredicates.PLURAL, context("100000.101"));
  }

  @Test
  public void testSingular() throws CodeException {
    assertFalse(CorePredicates.SINGULAR, context("0"));
    assertFalse(CorePredicates.SINGULAR, context("0.9"));
    // Integer part must be == 1
    assertTrue(CorePredicates.SINGULAR, context("1"));
    assertTrue(CorePredicates.SINGULAR, context("1.1"));
  }

  private void assertTrue(Predicate predicate, Context ctx) throws CodeException {
    assertTrue(predicate, ctx, Constants.EMPTY_ARGUMENTS);
  }

  private void assertTrue(Predicate predicate, Context ctx, Arguments args) throws CodeException {
    predicate.validateArgs(args);
    Assert.assertTrue(predicate.apply(ctx, args));
  }

  private void assertFalse(Predicate predicate, Context ctx) throws CodeException {
    assertFalse(predicate, ctx, EMPTY_ARGUMENTS);
  }

  private void assertFalse(Predicate predicate, Context ctx, Arguments args) throws CodeException {
    predicate.validateArgs(args);
    Assert.assertFalse(predicate.apply(ctx, args));
  }
}
