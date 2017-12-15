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
import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class ReferenceScannerTest extends UnitTestBase {

  private static final boolean VERBOSE = false;

  @Test
  public void testBasic() throws CodeException, JsonProcessingException {
    ObjectNode result = scan("{.section nums}{.even? @foo}#{@|json}{.end}{.even?}#{.end}{.end}");
    render(result);

    assertEquals(result.get("instructions").get("VARIABLE").asInt(), 1);
    assertEquals(result.get("instructions").get("SECTION").asInt(), 1);
    assertEquals(result.get("instructions").get("TEXT").asInt(), 2);
    assertEquals(result.get("instructions").get("PREDICATE").asInt(), 2);
    assertEquals(result.get("instructions").get("END").asInt(), 3);
    assertEquals(result.get("predicates").get("even?").asInt(), 2);
    assertEquals(result.get("formatters").get("json").asInt(), 1);
    assertTrue(result.get("variables").get("nums").isObject());
    assertEquals(result.get("textBytes").asInt(), 2);
    if (DEBUG) {
      String json = JsonUtils.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result);
      System.out.println(json);
    }
  }

  @Test
  public void testPredicates() throws CodeException {
    ObjectNode result = scan("{.even? 2}#{.or odd? foo}!{.end}");
    render(result);

    assertEquals(result.get("instructions").get("PREDICATE").asInt(), 1);
    assertEquals(result.get("instructions").get("OR_PREDICATE").asInt(), 1);
    assertEquals(result.get("instructions").get("TEXT").asInt(), 2);
  }

  @Test
  public void testFormatters() throws CodeException {
    ObjectNode result = scan("{a|json|html|json}{b|json}{c|html}");
    render(result);
    assertEquals(result.get("formatters").get("json").asInt(), 3);
    assertEquals(result.get("formatters").get("html").asInt(), 2);
  }

  @Test
  public void testInstructions() throws CodeException {
    ObjectNode result = scan("{.if a || b}{.newline}{.meta-left}{.space}{.or}{.section a}{.end}{.end}");
    render(result);

    result = scan("{.if even?}a{.or}b{.end}");
    render(result);
    assertEquals(result.get("predicates").get("even?").asInt(), 1);
  }

  @Test
  public void testSectionNesting() throws CodeException {
    ObjectNode result = scan("{.section a}{.section b}{.section c}{d}{.end}{.end}{.end}");
    render(result);

    ObjectNode vars = (ObjectNode)result.get("variables");
    assertTrue(vars.get("a").get("b").get("c").get("d").isNull());

    result = scan("{.section a}{.end}{.section a}{.end}");
    render(result);
    assertTrue(vars.get("a").isObject());
  }

  @Test
  public void testTextBytes() throws CodeException {
    ObjectNode result = scan("{.section a}abcde{.or}fghij{.end}");
    render(result);
    assertEquals(result.get("textBytes").asInt(), 10);
  }

  @Test
  public void testAlternates() throws CodeException {
    ObjectNode result = scan("{.odd? a}{a}{.or}{b}{.end}{.repeated section c}{.alternates with}{d}{.end}");
    render(result);

    ObjectNode vars = (ObjectNode)result.get("variables");
    assertTrue(vars.get("a").isNull());
    assertTrue(vars.get("b").isNull());
    assertTrue(vars.get("c").isObject());
    assertEquals(((ObjectNode)vars.get("c")).size(), 1);
    assertTrue(vars.get("c").get("d").isNull());
  }

  private ObjectNode scan(String source) throws CodeException {
    ReferenceScanner scanner = new ReferenceScanner();
    CompiledTemplate template = compiler().compile(source, false, false);
    scanner.extract(template.code());
    return scanner.references().report();
  }

  private void render(ObjectNode result) {
    try {
      String json = JsonUtils.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result);
      if (VERBOSE) {
        System.out.println(json);
      }
    } catch (JsonProcessingException e) {
      Assert.fail(e.getMessage());
    }
  }

}
