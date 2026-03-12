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
package com.squarespace.template.plugins.platform.i18n;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.template.CodeMachine;
import com.squarespace.template.Context;
import com.squarespace.template.Instruction;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;


public class MessageFormatterTest extends PlatformUnitTestBase {

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), MessageFormatterTest.class);

  @Test
  public void testTimeZonePerRender() throws Exception {
    String template = "{messages.event|message s}";
    ObjectNode root = (ObjectNode) JsonUtils.decode("{"
        + "\"website\": {\"timeZone\": \"America/New_York\"},"
        + "\"messages\": {\"event\": \"{0 datetime time:medium}\"},"
        + "\"s\": 1582129775000}");
    Context ctx = new Context(root);
    CodeMachine sink = machine();
    tokenizer(template, sink).consume();
    Instruction code = sink.getCode();

    // First render in New York.
    ctx.execute(code);
    assertEquals(ctx.buffer().toString(), "11:29:35\u202FAM");

    // Same context, new zone: the second render must use its own zone.
    ctx.buffer().setLength(0);
    ((ObjectNode) root.path("website")).put("timeZone", "Asia/Tokyo");
    ctx.execute(code);
    assertEquals(ctx.buffer().toString(), "1:29:35\u202FAM");
  }

  @Test
  public void testMessageFormatter() throws Exception {
    runner.run(
        "f-message-named-args.html",
        "f-message-literal-args.html",
        "f-message-literal-args-legacy.html",
        "f-message-subpath-1.html",
        "f-message-subpath-2.html",
        "f-message-subpath-3.html",
        "f-message-subpath-4.html",
//        "f-message-units-en-US.html",
//        "f-message-units-fr-FR.html",
        "f-message-datetime-interval-en-US.html",
        "f-message-datetime-interval-fr-FR.html",
        "f-message-plural-en-US.html",
        "f-message-plural-fr-FR.html",
        "f-message-plural-pl-PL.html"
    );
  }

}
