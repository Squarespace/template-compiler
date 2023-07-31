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

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;


/**
 * Parses template compiler test cases from a simple file format.
 */
public class TestCaseParser extends UnitTestBase {

  // Unused: private static final String TYPE_COMMENTS = "COMMENTS";
  private static final String TYPE_PROPERTIES = "PROPERTIES";
  private static final String TYPE_ERROR = "ERROR";
  private static final String TYPE_JSON = "JSON";
  private static final String TYPE_OUTPUT = "OUTPUT";
  private static final String TYPE_INJECT = "INJECT";
  private static final String TYPE_PARTIALS = "PARTIALS";
  private static final String TYPE_TEMPLATE = "TEMPLATE";
  private static final Pattern RE_LINES = Pattern.compile("\n");
  private static final Pattern RE_SECTION = Pattern.compile("^:([\\w-_]+)\\s*$");

  /**
   * Parses a source file into a valid test case instance.
   */
  public TestCase parseTest(String source) {
    Map<String, String> sections = parseSections(source);
    // Unused: String comments = sections.get(TYPE_COMMENTS);
    String props = sections.get(TYPE_PROPERTIES);
    String json = sections.get(TYPE_JSON);
    String template = sections.get(TYPE_TEMPLATE);
    String partials = sections.get(TYPE_PARTIALS);
    String inject = sections.get(TYPE_INJECT);

    Properties properties = new Properties();
    if (props != null) {
      try {
        properties.load(new StringReader(props));
      } catch (IOException e) {
        throw new AssertionError("Failed to parse test case properties: " + e.getMessage(), e);
      }
    }

    if (sections.containsKey(TYPE_OUTPUT)) {
      return new OutputTestCase(properties, json, template, partials, inject, sections.get(TYPE_OUTPUT));
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

    private final Properties properties;
    private final String json;
    private final String template;
    private final String partials;
    private final String inject;
    private final String output;

    OutputTestCase(Properties properties, String json, String template, String partials, String inject, String output) {
      assertSection(json, TYPE_JSON, TYPE_OUTPUT);
      assertSection(template, TYPE_TEMPLATE, TYPE_OUTPUT);
      this.properties = properties;
      this.json = json;
      this.template = template;
      this.partials = partials;
      this.inject = inject;
      this.output = output.trim();
    }

    @Override
    public void run(Compiler compiler) {
      // Allow tests to be skipped for certain jdk versions
      String major = getJavaMajorVersion();

      // Execute the test for only the selected jdk versions
      String jdk = properties.getProperty("jdk");
      if (jdk != null && !listContains(major, jdk)) {
        return;
      }

      // Skip the test for the selected jdk versions
      String nojdk = properties.getProperty("nojdk");
      if (nojdk != null && listContains(major, nojdk)) {
        return;
      }

      String now = properties.getProperty("now");

      try {
        ObjectNode partialsMap = null;
        if (partials != null) {
          partialsMap = (ObjectNode) JsonUtils.decode(partials.trim());
        }
        ObjectNode injectMap = null;
        if (inject != null) {
          injectMap = (ObjectNode) JsonUtils.decode(inject.trim());
        }

        boolean preprocess = Boolean.valueOf(properties.getProperty("preprocess"));

        Instruction code = compiler.compile(template, false, preprocess).code();
        CompilerExecutor executor = compiler.newExecutor()
            .code(code)
            .json(json)
            .safeExecution(true);
        if (now != null) {
          executor.now(Long.valueOf(now));
        }
        if (partialsMap != null) {
          executor.partialsMap(partialsMap);
        }
        if (injectMap != null) {
          executor.injectablesMap(injectMap);
        }


        String locale = properties.getProperty("locale");
        if (locale != null) {
          java.util.Locale javaLocale = java.util.Locale.forLanguageTag(locale);
          executor.locale(javaLocale);
        }

        Context ctx = executor.execute();
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
        buf.append("- ").append(escaped(row)).append('\n');
      }
      for (String row : lines2) {
        buf.append("+ ").append(escaped(row)).append('\n');
      }
    }
    return buf.toString();
  }

  private static final char[] HEX = new char[] {
      '0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  private static String escaped(String src) {
    byte[] bytes = src.getBytes(Charset.forName("UTF-8"));
    StringBuilder buf = new StringBuilder();
    for (byte b : bytes) {
      if (b < 0x21 || b >= 0x7f) {
        buf.append("\\x");
        buf.append(HEX[b >>> 4]);
        buf.append(HEX[b & 0xF]);
      } else {
        buf.append((char)b);
      }
    }
    return buf.toString();
  }

  private static boolean listContains(String needle, String haystack) {
    String major = getJavaMajorVersion();
    long count = Arrays.stream(StringUtils.split(haystack, ','))
        .map(s -> s.trim().equals(major))
        .count();
    return count != 0;
  }

  private static String getJavaMajorVersion() {
    String version = System.getProperty("java.version");
    return version.substring(0, version.indexOf('.'));
  }
}
