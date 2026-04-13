/**
 * Copyright (c) 2017 SQUARESPACE, Inc.
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

import java.util.Map;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonDiff;


public class AstEmitterTest extends UnitTestBase {

  @Test
  public void testAst() throws Exception {
    execute("ast-1.html");
    execute("ast-2.html");
    execute("ast-3.html");
    execute("ast-4.html");
    execute("ast-5.html");
    execute("ast-6.html");
    execute("ast-7.html");
    execute("ast-8.html");
    execute("ast-9.html");
    execute("ast-10.html");
    execute("ast-11.html");
    execute("ast-12.html");
    execute("ast-13.html");
    execute("ast-14.html");
    execute("ast-15.html");
    execute("ast-16.html");
    execute("ast-17.html");
    execute("ast-18.html");
  }

  @Test
  public void testRoundTrip() throws Exception {
    String source = GeneralUtils.loadResource(AstEmitterTest.class, "roundtrip-1.html");
    String out = render(source);
    assertTrue(out.startsWith("[17,"));
    assertTrue(out.endsWith(",18]"));
  }

  @Test
  public void testFormatterShapeNoArgs() throws Exception {
    // No-arg formatter: {x|mod} emits ["<name>"] (see AstEmitter.formatters()).
    JsonNode formatters = formatters("{x|mod}");
    assertEquals(formatters.size(), 1);
    assertEquals(formatters.get(0).toString(), "[\"mod\"]");
  }

  @Test
  public void testFormatterShapeWithArgs() throws Exception {
    // One-arg formatter: {x|mod 3} emits ["<name>", [["<arg>"], "<delimiter>"]]
    // per the arguments() contract.
    JsonNode formatters = formatters("{x|mod 3}");
    assertEquals(formatters.size(), 1);
    assertEquals(formatters.get(0).toString(), "[\"mod\",[[\"3\"],\" \"]]");
  }

  @Test
  public void testFormatterShapeChain() throws Exception {
    // Chained no-arg formatters each emit a single-element array.
    JsonNode formatters = formatters("{x|mod|raw}");
    assertEquals(formatters.size(), 2);
    assertEquals(formatters.get(0).toString(), "[\"mod\"]");
    assertEquals(formatters.get(1).toString(), "[\"raw\"]");
  }

  private JsonNode formatters(String source) throws CodeSyntaxException {
    CompiledTemplate template = compiler().compile(source);
    // ROOT is [17, 1, [instructions], 18]; the first instruction is the
    // VARIABLE node [1, [variables], formatters].
    return AstEmitter.get(template.code()).get(2).get(0).get(2);
  }

  private String render(String source) throws CodeSyntaxException {
    CompiledTemplate template = compiler().compile(source);
    return AstEmitter.get(template.code()).toString();
  }

  private void execute(String path) throws Exception {
    String raw = GeneralUtils.loadResource(AstEmitterTest.class, path);
    Map<String, String> sections = TestCaseParser.parseSections(raw);
    String source = sections.get("SOURCE").trim();
    CompiledTemplate template = compiler().compile(source);

    String json = sections.get("JSON").trim();
    JsonNode expected = JsonUtils.decode(json);

    JsonNode actual = AstEmitter.get(template.code());

    // TODO: improve the diff formatting.
    String diff = JsonDiff.asJson(expected, actual).toString();
    assertEquals(actual.toString(), expected.toString(), path + ": " + diff);
  }

}
