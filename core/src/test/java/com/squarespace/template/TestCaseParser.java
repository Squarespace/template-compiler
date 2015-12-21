/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
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

import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.node.ObjectNode;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;


/**
 * Parses template compiler test cases from a simple file format.
 */
public class TestCaseParser extends UnitTestBase {

  private static final String TYPE_ERROR = "ERROR";

  private static final String TYPE_JSON = "JSON";

  private static final String TYPE_OUTPUT = "OUTPUT";

  private static final String TYPE_PARTIALS = "PARTIALS";

  private static final String TYPE_TEMPLATE = "TEMPLATE";

  private static final Pattern RE_LINES = Pattern.compile("\n");

  private static final Pattern RE_SECTION = Pattern.compile("^:([\\w-_]+)\\s*$");

  /**
   * Parses a source file into a valid test case instance.
   */
  public TestCase parseTest(String source) {
    Map<String, String> sections = parseSections(source);
    String json = sections.get(TYPE_JSON);
    String template = sections.get(TYPE_TEMPLATE);
    String partials = sections.get(TYPE_PARTIALS);
    if (sections.containsKey(TYPE_OUTPUT)) {
      return new OutputTestCase(json, template, partials, sections.get(TYPE_OUTPUT));
    } else if (sections.containsKey(TYPE_ERROR)) {
// TBD:     return new ErrorCase(json, template, sections.get(TYPE_ERROR));
    }
    throw new AssertionError("Expected one of: " + TYPE_ERROR + ", " + TYPE_OUTPUT);
  }

  public interface TestCase {
    void run(Compiler compiler);
  }

  /**
   * A test case that executes a template and asserts some output was produced.
   * Produces a diff showing where the output is invalid.
   */
  private class OutputTestCase implements TestCase {

    private final String json;

    private final String template;

    private final String partials;

    private final String output;

    OutputTestCase(String json, String template, String partials, String output) {
      assertSection(json, TYPE_JSON, TYPE_OUTPUT);
      assertSection(template, TYPE_TEMPLATE, TYPE_OUTPUT);
      this.json = json;
      this.template = template;
      this.partials = partials;
      this.output = output.trim();
    }

    @Override
    public void run(Compiler compiler) {
      try {
        ObjectNode partialsMap = null;
        if (partials != null) {
          partialsMap = (ObjectNode) JsonUtils.decode(partials);
        }
        Instruction code = compiler.compile(template, false).code();
        Context ctx = compiler.newExecutor()
            .code(code)
            .json(json)
            .partialsMap(partialsMap)
            .safeExecution(true)
            .execute();
        String actual = ctx.buffer().toString().trim();
        if (!output.equals(actual)) {
          throw new AssertionError("Output does not match:\n" + diff(output, actual));
        }
      } catch (Exception e) {
        throw new AssertionError("Error running test case", e);
      }
    }

  }


  // TBD: ErrorCase implementation


  private static void assertSection(String value, String sectionType, String caseType) {
    assertNotNull(value, caseType + " case requires a valid '" + sectionType + "' section");
  }

  /**
   * Parses a simplistic format of sections annotated by keys:
   *
   *   :<KEY-1>
   *   [ one or more lines ]
   *
   *   :<KEY-n>
   *   [ one or more lines ]
   */
  public static Map<String, String> parseSections(String source) {
    Matcher matcher = RE_SECTION.matcher("");
    String[] lines = RE_LINES.split(source);

    Map<String, String> sections = new HashMap<>();
    String key = null;
    StringBuilder buf = new StringBuilder();

    int size = lines.length;
    for (int i = 0; i < size; i++) {
      matcher.reset(lines[i]);
      if (matcher.lookingAt()) {
        if (key != null) {
          sections.put(key, buf.toString());
          buf.setLength(0);
        }
        key = matcher.group(1);

      } else {
        buf.append('\n');
        buf.append(lines[i]);
      }
    }

    if (!sections.containsKey(key)) {
      sections.put(key, buf.toString());
    }
    return sections;
  }

  /**
   * Return a string showing the differences between the expected and actual
   * parameters.
   */
  public static String diff(String expected, String actual) {
    List<String> expList = Arrays.asList(expected.split("\n"));
    List<String> actList = Arrays.asList(actual.split("\n"));
    Patch<String> patch = DiffUtils.diff(expList, actList);
    List<Delta<String>> deltas = patch.getDeltas();
    if (deltas.size() == 0) {
      return null;
    }
    StringBuilder buf = new StringBuilder();
    for (Delta<String> delta : deltas) {
      Chunk<String> chunk1 = delta.getOriginal();
      int pos1 = chunk1.getPosition();
      List<String> lines1 = chunk1.getLines();

      Chunk<String> chunk2 = delta.getRevised();
      int pos2 = chunk2.getPosition();
      List<String> lines2 = chunk2.getLines();

      buf.append("@@ -" + pos1 + "," + lines1.size());
      buf.append(" +" + pos2 + "," + lines2.size()).append(" @@\n");
      for (String row : lines1) {
        buf.append("- ").append(row).append('\n');
      }
      for (String row : lines2) {
        buf.append("+ ").append(row).append('\n');
      }
    }
    return buf.toString();
  }

}
