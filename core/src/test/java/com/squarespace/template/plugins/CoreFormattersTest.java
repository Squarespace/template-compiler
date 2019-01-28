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

import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_MISSING;
import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_RECURSION_DEPTH;
import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_SYNTAX;
import static com.squarespace.template.ExecuteErrorType.COMPILE_PARTIAL_SYNTAX;
import static com.squarespace.template.KnownDates.MAY_13_2013_010000_UTC;
import static com.squarespace.template.KnownDates.NOV_15_2013_123030_UTC;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Locale;

import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.CodeBuilder;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.Instruction;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.SyntaxErrorType;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.UnitTestBase;
import com.squarespace.template.plugins.CoreFormatters.ApplyFormatter;
import com.squarespace.template.plugins.CoreFormatters.CountFormatter;
import com.squarespace.template.plugins.CoreFormatters.CycleFormatter;
import com.squarespace.template.plugins.CoreFormatters.EncodeSpaceFormatter;
import com.squarespace.template.plugins.CoreFormatters.EncodeUriComponentFormatter;
import com.squarespace.template.plugins.CoreFormatters.EncodeUriFormatter;
import com.squarespace.template.plugins.CoreFormatters.HtmlAttrFormatter;
import com.squarespace.template.plugins.CoreFormatters.HtmlFormatter;
import com.squarespace.template.plugins.CoreFormatters.HtmlTagFormatter;
import com.squarespace.template.plugins.CoreFormatters.JsonFormatter;
import com.squarespace.template.plugins.CoreFormatters.JsonPrettyFormatter;
import com.squarespace.template.plugins.CoreFormatters.LookupFormatter;
import com.squarespace.template.plugins.CoreFormatters.OutputFormatter;
import com.squarespace.template.plugins.CoreFormatters.PluralizeFormatter;
import com.squarespace.template.plugins.CoreFormatters.RawFormatter;
import com.squarespace.template.plugins.CoreFormatters.RoundFormatter;
import com.squarespace.template.plugins.CoreFormatters.SafeFormatter;
import com.squarespace.template.plugins.CoreFormatters.SlugifyFormatter;
import com.squarespace.template.plugins.CoreFormatters.SmartypantsFormatter;
import com.squarespace.template.plugins.CoreFormatters.StrFormatter;
import com.squarespace.template.plugins.CoreFormatters.TruncateFormatter;
import com.squarespace.template.plugins.CoreFormatters.UrlEncodeFormatter;


@Test(groups = { "unit" })
public class CoreFormattersTest extends UnitTestBase {

  private static final Formatter APPLY = new ApplyFormatter();
  private static final Formatter COUNT = new CountFormatter();
  private static final Formatter CYCLE = new CycleFormatter();
  private static final Formatter ENCODE_SPACE = new EncodeSpaceFormatter();
  private static final Formatter ENCODE_URI = new EncodeUriFormatter();
  private static final Formatter ENCODE_URI_COMPONENT = new EncodeUriComponentFormatter();
  private static final Formatter HTML = new HtmlFormatter();
  private static final Formatter HTMLATTR = new HtmlAttrFormatter();
  private static final Formatter HTMLTAG = new HtmlTagFormatter();
  private static final Formatter JSON = new JsonFormatter();
  private static final Formatter JSON_PRETTY = new JsonPrettyFormatter();
  private static final Formatter OUTPUT = new OutputFormatter();
  private static final Formatter LOOKUP = new LookupFormatter();
  private static final Formatter PLURALIZE = new PluralizeFormatter();
  private static final Formatter RAW = new RawFormatter();
  private static final Formatter ROUND = new RoundFormatter();
  private static final Formatter SAFE = new SafeFormatter();
  private static final Formatter SLUGIFY = new SlugifyFormatter();
  private static final Formatter SMARTYPANTS = new SmartypantsFormatter();
  private static final Formatter STR = new StrFormatter();
  private static final Formatter TRUNCATE = new TruncateFormatter();
  private static final Formatter URL_ENCODE = new UrlEncodeFormatter();

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), CoreFormattersTest.class);

  @Test
  public void testApply() {
    runner.run("f-apply-1.html");
  }

  @Test
  public void testApplyMacros() {
    runner.run(
        "f-macro-1.html",
        "f-macro-2.html",
        "f-macro-3.html",
        "f-macro-4.html",
        "f-macro-5.html",
        "f-macro-6.html",
        "f-macro-7.html"
    );
  }

  @Test
  public void testApplyPartial() throws CodeException {
    String partials = "{\"block.item\": \"this {@} value\"}";
    String input = "{\"foo\": 123}";

    CodeMaker mk = maker();
    CodeBuilder cb = builder().text("hi ");
    Arguments args = mk.args(" block.item");
    APPLY.validateArgs(args);
    cb.var("foo", mk.fmt(APPLY, args));
    Instruction root = cb.text("!").eof().build();

    Context ctx = new Context(JsonUtils.decode(input));
    ctx.setCompiler(compiler());
    ctx.setPartials(JsonUtils.decode(partials));
    ctx.execute(root);
    assertContext(ctx, "hi this 123 value!");
  }

  @Test
  public void testApplyPartialError() throws CodeException {
    String template = "{@|apply block}";
    String partials = "{\"block\": \"{.section foo}{@}\"}";
    String input = "{\"foo\": 123}";
    try {
      compiler().newExecutor()
          .template(template)
          .json(input)
          .partialsMap((ObjectNode)JsonUtils.decode(partials))
          .execute();
      fail("Expected exception.");
    } catch (CodeExecuteException e) {
      assertEquals(e.getErrorInfo().getType(), APPLY_PARTIAL_SYNTAX);
    }
  }

  @Test
  public void testApplyPartialErrorSafe() throws CodeException {
    String template = "{@|apply block}";
    String partials = "{\"block\": \"{@|no-formatter}\"}";
    Context ctx = compiler().newExecutor()
        .template(template)
        .partialsMap(((ObjectNode)JsonUtils.decode(partials)))
        .json("123")
        .safeExecution(true)
        .execute();
    assertEquals(ctx.getErrors().size(), 1);
    assertEquals(ctx.getErrors().get(0).getType(), COMPILE_PARTIAL_SYNTAX);
  }

  @Test
  public void testApplyPartialMissing() throws CodeException {
    String template = "{@|apply foo}";
    String partials = "{\"block\": \"hello\"}";
    String input = "{}";
    Instruction inst = compiler().compile(template).code();
    Context ctx = new Context(JsonUtils.decode(input));
    ctx.setCompiler(compiler());
    ctx.setPartials(JsonUtils.decode(partials));
    try {
      ctx.execute(inst);
      fail("Expected exception");
    } catch (CodeExecuteException e) {
      assertEquals(e.getErrorInfo().getType(), APPLY_PARTIAL_MISSING);
    }
  }

  @Test
  public void testApplyPartialMissingSafe() throws CodeException {
    Context ctx = compiler().newExecutor()
        .template("{@|apply foo}")
        .json("{}")
        .partialsMap((ObjectNode)JsonUtils.decode("{}"))
        .safeExecution(true)
        .execute();

    assertEquals(ctx.getErrors().size(), 1);
    assertEquals(ctx.getErrors().get(0).getType(), APPLY_PARTIAL_MISSING);
  }

  @Test
  public void testApplyPartialErrorSyntax() throws CodeException {
    String template = "{@|apply block}";
    String input = "{}";
    String partials = "{\"block\": \"{.section foo}{@}\"}";
    Instruction inst = compiler().compile(template).code();
    Context ctx = compiler().newExecutor()
        .json(input)
        .code(inst)
        .partialsMap(partials)
        .safeExecution(true)
        .execute();
    assertContext(ctx, "");
    assertEquals(ctx.getErrors().size(), 1);
    assertEquals(ctx.getErrors().get(0).getType(), COMPILE_PARTIAL_SYNTAX);
  }

  @Test
  public void testApplyPartialPrivate() throws CodeException {
    // The "private" argument hides the entire parent context from the partial
    String template = "{child|apply block private}{child|apply block}";
    String partials = "{\"block\": \"hello {name}\"}";
    String input = "{\"name\": \"bob\", \"child\": {}}";
    Instruction inst = compiler().compile(template).code();
    Context ctx = compiler().newExecutor()
        .json(input)
        .code(inst)
        .partialsMap(partials)
        .safeExecution(true)
        .execute();
    assertContext(ctx, "hello hello bob");
  }

  @Test
  public void testApplyPartialRecursion() throws CodeException {
    String template = "{@|apply one}";
    String partials = "{\"one\": \"1{@|apply two}\", \"two\": \"2{@|apply one}\"}";
    String input = "{}";
    Instruction inst = compiler().compile(template).code();
    Context ctx = compiler().newExecutor()
        .json(input)
        .code(inst)
        .partialsMap(partials)
        .maxPartialDepth(6)
        .safeExecution(true)
        .execute();
    assertContext(ctx, "121212");
    assertEquals(ctx.getErrors().size(), 1);
    assertEquals(ctx.getErrors().get(0).getType(), APPLY_PARTIAL_RECURSION_DEPTH);
  }

  @Test
  public void testApplyPartialNested() throws CodeException {
    String template = "{@|apply one}";
    String partials = "{\"one\": \"1{@|apply two}\", \"two\": \"2{@|apply three}\", "
        + "\"three\": \"3{@|apply four}\", \"four\": \"4\"}";
    String input = "{}";
    Instruction inst = compiler().compile(template).code();
    Context ctx = compiler().newExecutor()
        .json(input)
        .code(inst)
        .partialsMap(partials)
        .safeExecution(true)
        .execute();
    assertContext(ctx, "1234");
  }

  @Test
  public void testApplyPartialNestedRecursion() throws CodeException {
    String template = "{@|apply one}";
    String partials = "{\"one\": \"1{@|apply two}\", \"two\": \"2{@|apply three}\", "
        + "\"three\": \"3{@|apply four}\", \"four\": \"4{@|apply one}\"}";
    String input = "{}";
    Instruction inst = compiler().compile(template).code();
    Context ctx = compiler().newExecutor()
        .json(input)
        .code(inst)
        .partialsMap(partials)
        .safeExecution(true)
        .execute();
    assertContext(ctx, "1234123412341234");
    assertEquals(ctx.getErrors().size(), 1);
    assertEquals(ctx.getErrors().get(0).getType(), APPLY_PARTIAL_RECURSION_DEPTH);
  }

  @Test
  public void testApplyPartialRecursionDepth() throws CodeException {
    String template = "{@|apply one}";
    String partials = "{\"one\": \"1{@|apply two}\", \"two\": \"2{@|apply three}\", "
        + "\"three\": \"3{@|apply four}\", \"four\": \"4\"}";
    String input = "{}";
    Instruction inst = compiler().compile(template).code();
    Context ctx = compiler().newExecutor()
        .json(input)
        .code(inst)
        .partialsMap(partials)
        .safeExecution(true)
        .maxPartialDepth(3)
        .execute();
    assertContext(ctx, "123");
    assertEquals(ctx.getErrors().size(), 1);
    assertEquals(ctx.getErrors().get(0).getType(), APPLY_PARTIAL_RECURSION_DEPTH);
  }

  @Test
  public void testCount() throws CodeException {
    for (String val : new String[] { "null", "0", "\"foo\"" }) {
      assertFormatter(COUNT, val, "0");
    }
    assertFormatter(COUNT, "[1,2,3]", "3");
    assertFormatter(COUNT, "{\"a\": 1, \"b\": 2}", "2");
  }

  @Test
  public void testCycle() throws CodeException {
    CodeMaker mk = maker();
    Arguments args = mk.args(" A B C");
    String result = "";
    for (int i = -6; i < 6; i++) {
      result += format(CYCLE, args, Integer.toString(i));
    }
    assertEquals("CABCABCABCAB", result);
  }

  @Test
  public void testDate() throws CodeException {
    String tzNewYork = "America/New_York";
    String tzLosAngeles = "America/Los_Angeles";
    String format = "%Y-%m-%d %H:%M:%S %Z";
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, "UTC"), "2013-05-13 01:00:00 UTC");
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, tzNewYork), "2013-05-12 21:00:00 EDT");
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, tzLosAngeles), "2013-05-12 18:00:00 PDT");
    assertEquals(formatDate(format, NOV_15_2013_123030_UTC, tzNewYork), "2013-11-15 07:30:30 EST");
    assertEquals(formatDate(format, NOV_15_2013_123030_UTC, tzLosAngeles), "2013-11-15 04:30:30 PST");

    String r1 = formatDate("%a %d %b %Y %I:%M:%S %p %Z", NOV_15_2013_123030_UTC, tzNewYork);
    String r2 = formatDate("%c", NOV_15_2013_123030_UTC, tzNewYork);
    assertEquals(r1, r2);

    r1 = formatDate("%a %d %b %Y %I:%M:%S %p %Z", MAY_13_2013_010000_UTC, tzNewYork);
    r2 = formatDate("%c", MAY_13_2013_010000_UTC, tzNewYork);
    assertEquals(r1, r2);


    r1 = formatDate("%F", NOV_15_2013_123030_UTC, tzLosAngeles);
    r2 = formatDate("%Y-%m-%d", NOV_15_2013_123030_UTC, tzLosAngeles);
    assertEquals(r1, r2);

    r1 = formatDate("%D", NOV_15_2013_123030_UTC, tzNewYork);
    r2 = formatDate("%m/%d/%y", NOV_15_2013_123030_UTC, tzNewYork);
    assertEquals(r1, r2);

    r1 = formatDate("%x", NOV_15_2013_123030_UTC, tzNewYork);
    r2 = formatDate("%m/%d/%Y", NOV_15_2013_123030_UTC, tzNewYork);
    assertEquals(r1, r2);

    r1 = formatDate("%R", NOV_15_2013_123030_UTC, tzNewYork);
    r2 = formatDate("%H:%M", NOV_15_2013_123030_UTC, tzNewYork);
    assertEquals(r1, r2);

    r1 = formatDate("%X", NOV_15_2013_123030_UTC, tzNewYork);
    r2 = formatDate("%I:%M:%S %p", NOV_15_2013_123030_UTC, tzNewYork);
    assertEquals(r1, r2);

    r1 = formatDate("%X", MAY_13_2013_010000_UTC, tzNewYork);
    r2 = formatDate("%I:%M:%S %p", MAY_13_2013_010000_UTC, tzNewYork);
    assertEquals(r1, r2);

    // am/pm and 12-hour hour
    format = "%p %P %l";
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, tzNewYork), "PM pm  9");
    assertEquals(formatDate(format, NOV_15_2013_123030_UTC, tzNewYork), "AM am  7");

    // Timezone short names and offsets
    format = "%Z %z";
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, tzNewYork), "EDT -0400");
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, tzLosAngeles), "PDT -0700");
    assertEquals(formatDate(format, NOV_15_2013_123030_UTC, tzNewYork), "EST -0500");
    assertEquals(formatDate(format, NOV_15_2013_123030_UTC, tzLosAngeles), "PST -0800");

    // TODO: Week of Year (Joda doesn't support the full range of week-of-year calculations)
//    format = "%U %V %W";
//    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, tzNewYork), "20");
  }

  @Test
  public void testDateLocale() throws CodeException {
    String tzNewYork = "America/New_York";
    String format = "%B %d";

    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, tzNewYork, Locale.GERMANY), "Mai 12");
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, tzNewYork, Locale.FRENCH), "mai 12");
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, tzNewYork, new Locale("es", "MX")), "mayo 12");
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, tzNewYork, new Locale("es", "SP")), "mayo 12");

    format = "%A";
    assertEquals(formatDate(format, NOV_15_2013_123030_UTC, tzNewYork, Locale.GERMANY), "Freitag");
    assertEquals(formatDate(format, NOV_15_2013_123030_UTC, tzNewYork, new Locale("es", "MX")), "viernes");
  }

  private String formatDate(String format, long timestamp, String tzId) throws CodeException {
    return formatDate(format, timestamp, tzId, Locale.US);
  }

  private String formatDate(String format, long timestamp, String tzId, Locale locale) throws CodeException {
    String template = "{time|date " + format + "}";
    String json = getDateTestJson(timestamp, tzId);
    Instruction code = compiler().compile(template).code();
    Context ctx = compiler().newExecutor()
        .code(code)
        .json(json)
        .locale(locale)
        .execute();
    return eval(ctx);
  }

  /**
   * Ensure that partials can see global scope.
   */
  @Test
  public void testDatePartial() throws CodeException {
    String tzNewYork = "America/New_York";
    String format = "%Y-%m-%d %H:%M:%S %Z";

    String json = getDateTestJson(MAY_13_2013_010000_UTC, tzNewYork);
    String template = "{@|apply block}";
    String partials = "{\"block\": \"{time|date " + format + "}\"}";

    Context ctx = new Context(JsonUtils.decode(json));
    ctx.setCompiler(compiler());
    ctx.setPartials(JsonUtils.decode(partials));

    Instruction inst = compiler().compile(template).code();
    ctx.execute(inst);
    assertContext(ctx, "2013-05-12 21:00:00 EDT");
  }

  @Test
  public void testDateNoTimeZone() throws CodeException {
    Context ctx = compiler().newExecutor()
        .template("{@|date %Y}")
        .json("167200000")
        .execute();
    assertEquals(ctx.buffer().toString(), "1970");
  }

  @Test
  public void testEncodeSpace() throws CodeException {
    assertFormatter(ENCODE_SPACE, "\"  \\n \"", "&nbsp;&nbsp;&nbsp;&nbsp;");
  }

  @Test
  public void testEscapeUri() throws CodeException {
    assertFormatter(ENCODE_URI, "\"<=%>\"", "%3c=%25%3e");
  }

  @Test
  public void testEscapeUriComponent() throws CodeException {
    assertFormatter(ENCODE_URI_COMPONENT, "\"<=%>\"", "%3c%3d%25%3e");
  }

  @Test
  public void testFormat() throws CodeException {
    runner.run(
        "f-format-1.html",
        "f-format-2.html",
        "f-format-3.html"
    );
  }

  @Test
  public void testIter() throws CodeException {
    String template = "{.repeated section foo}{@|iter}{.end}";
    String input = "{\"foo\": [\"a\", \"b\", \"c\"]}";

    Instruction inst = compiler().compile(template).code();
    Context ctx = new Context(JsonUtils.decode(input));
    ctx.setCompiler(compiler());
    ctx.execute(inst);
    assertContext(ctx, "123");
  }

  @Test
  public void testHtmlEscape() throws CodeException {
    assertFormatter(HTML, "\"< foo & bar >\"", "&lt; foo &amp; bar &gt;");
    for (Formatter formatter : Arrays.asList(HTMLTAG, HTMLATTR)) {
      assertFormatter(formatter, "\"< \\\"foo & bar\\\" >\"", "&lt; &quot;foo &amp; bar&quot; &gt;");
    }
  }

  @Test
  public void testJson() throws CodeException {
    assertFormatter(JSON, "{ \"a\":  3.14159 }", "{\"a\":3.14159}");
    assertFormatter(JSON, "\"foo </script>\"", "\"foo <\\/script>\"");

    assertFormatter(JSON_PRETTY, "{ \"a\":  3.14159 }", "{\n  \"a\" : 3.14159\n}");

    runner.run(
        "f-json-1.html",
        "f-json-2.html",
        "f-json-3.html",
        "f-json-4.html",
        "f-json-5.html",

        "f-json-pretty-1.html",
        "f-json-pretty-2.html"
    );
  }

  @Test
  public void testOutput() throws CodeException {
    CodeMaker mk = maker();
    Arguments args = mk.args(":1:2:3");
    assertFormatter(OUTPUT, args, "{}", "1 2 3");
  }

  @Test
  public void testLookup() throws CodeException {
    CodeMaker mk = maker();
    Arguments args = mk.args(" var");
    assertFormatter(LOOKUP, args, "{\"var\": \"foo\", \"foo\": 123}", "123");
    assertFormatter(LOOKUP, args, "{\"var\": \"bar\", \"foo\": 123}", "");
    assertFormatter(LOOKUP, args, "{\"var\": \"bar\", \"foo\": 123, \"bar\": 456}", "456");
    assertFormatter(LOOKUP, args, "{\"var\": \"foo.bar\", \"foo\": {\"bar\": 123}}", "123");
    assertFormatter(LOOKUP, args, "{\"var\": \"data.1\", \"data\": [123, 456]}", "456");
    assertFormatter(LOOKUP, args, "{\"var\": \"foo.bar.0\", \"foo\": {\"bar\": [123]}}", "123");

    Context ctx = compiler().newExecutor()
        .json("{\"foo\": {\"bar\": 123}, \"baz\": \"bar\"}")
        .template("{foo|lookup baz}")
        .safeExecution(true)
        .execute();
    assertContext(ctx, "123");
  }

  @Test
  public void testPluralize() throws CodeException {
    CodeMaker mk = maker();

    Arguments args = mk.args("");
    assertFormatter(PLURALIZE, "0", "s");
    assertFormatter(PLURALIZE, "1", "");
    assertFormatter(PLURALIZE, "2", "s");
    assertFormatter(PLURALIZE, "3.1415", "s");

    args = mk.args(" A");
    assertFormatter(PLURALIZE, args, "0", "A");
    assertFormatter(PLURALIZE, args, "1", "");
    assertFormatter(PLURALIZE, args, "2", "A");
    assertFormatter(PLURALIZE, args, "100", "A");

    args = mk.args("/A/B");
    assertFormatter(PLURALIZE, args, "0", "B");
    assertFormatter(PLURALIZE, args, "1", "A");
    assertFormatter(PLURALIZE, args, "2", "B");
    assertFormatter(PLURALIZE, args, "100", "B");

    // Too many args
    args = mk.args(":1:2:3:4");
    assertInvalidArgs(PLURALIZE, args);
  }

  @Test
  public void testRaw() throws CodeException {
    for (String json : new String[] { "123", "3.14159", "-12", "false", "true", "null", "\"abc\"" }) {
      assertFormatter(RAW, json, json);
    }
  }

  @Test
  public void testRound() throws CodeException {
    CodeMaker mk = maker();
    Arguments args = mk.args("");
    assertFormatter(ROUND, args, "1.44", "1");
    assertFormatter(ROUND, args, "1.6", "2");
  }

  @Test
  public void testSafe() throws CodeException {
    assertFormatter(SAFE, "\"foo <bar> bar\"", "foo  bar");
    assertFormatter(SAFE, "\"<script\\nsrc=\\\"url\\\"\\n>foobar</script>\"", "foobar");
    assertFormatter(SAFE, "\"<div>\\n<b>\\nfoobar\\n</b>\\n</div>\"", "\n\nfoobar\n\n");
    assertFormatter(SAFE, "{}", "");
  }

  @Test
  public void testSlugify() throws CodeException {
    String data = "\"Next Total Eclipse on 20th of March 2015\"";
    assertFormatter(SLUGIFY, data, "next-total-eclipse-on-20th-of-march-2015");
    data = "\"Value of PI is approx. 3.14159\"";
    assertFormatter(SLUGIFY, data, "value-of-pi-is-approx-314159");
    data = "\"1.2.3.4.5-()*&-foo.bar-baz\"";
    assertFormatter(SLUGIFY, data, "12345--foobar-baz");
  }

  @Test
  public void testSmartypants() throws CodeException {
    assertFormatter(SMARTYPANTS, "\"Fred's\"", "Fred\u2019s");
    assertFormatter(SMARTYPANTS, "\"\\\"foo\\\"\"", "\u201cfoo\u201d");
  }

  @Test
  public void testStr() throws CodeException {
    for (String json : new String[] { "123", "3.14159", "-12", "false", "true" }) {
      assertFormatter(STR, json, json);
    }
    assertFormatter(STR, "\"abc\"", "abc");
    assertFormatter(STR, "null", "");
  }

  @Test
  public void testTruncate() throws CodeException {
    CodeMaker mk = maker();
    assertFormatter(TRUNCATE, mk.args(""), "\"foo\"", "foo");
    assertFormatter(TRUNCATE, mk.args(" 5 .."), "\"foo bar baz\"", "foo ..");
    assertFormatter(TRUNCATE, mk.args(" 100 .."), "\"foo bar baz\"", "foo bar baz");

    Context ctx = compiler().newExecutor()
        .template("{@|truncate xyz}")
        .safeExecution(true)
        .execute();
    assertEquals(ctx.getErrors().size(), 1);
    assertEquals(ctx.getErrors().get(0).getType(), SyntaxErrorType.FORMATTER_ARGS_INVALID);
  }

  @Test
  public void testUrlEncode() throws CodeException {
    assertFormatter(URL_ENCODE, "\"\u201ca b\u201d\"", "%E2%80%9Ca+b%E2%80%9D");
  }

  protected static String getDateTestJson(long timestamp, String tzId) {
    DateTimeZone timezone = DateTimeZone.forID(tzId);
    ObjectNode node = JsonUtils.createObjectNode();
    node.put("time", timestamp);
    ObjectNode website = node.putObject("website");
    website.put("timeZoneOffset", timezone.getOffset(timestamp));
    website.put("timeZone", timezone.getID());
    return node.toString();
  }
}
