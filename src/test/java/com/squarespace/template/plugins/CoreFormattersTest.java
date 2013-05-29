package com.squarespace.template.plugins;

import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_SYNTAX;
import static com.squarespace.template.KnownDates.MAY_13_2013_010000_UTC;
import static com.squarespace.template.KnownDates.NOV_15_2013_123030_UTC;
import static com.squarespace.template.plugins.CoreFormatters.ENCODE_SPACE;
import static com.squarespace.template.plugins.CoreFormatters.HTML;
import static com.squarespace.template.plugins.CoreFormatters.HTMLATTR;
import static com.squarespace.template.plugins.CoreFormatters.HTMLTAG;
import static com.squarespace.template.plugins.CoreFormatters.JSON;
import static com.squarespace.template.plugins.CoreFormatters.JSON_PRETTY;
import static com.squarespace.template.plugins.CoreFormatters.PLURALIZE;
import static com.squarespace.template.plugins.CoreFormatters.RAW;
import static com.squarespace.template.plugins.CoreFormatters.SLUGIFY;
import static com.squarespace.template.plugins.CoreFormatters.TIMESINCE;
import static com.squarespace.template.plugins.CoreFormatters.TRUNCATE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;

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
import com.squarespace.template.UnitTestBase;
import com.squarespace.v6.utils.JSONUtils;


@Test( groups={ "unit" })
public class CoreFormattersTest extends UnitTestBase {

  @Test
  public void testAbsUrl() throws CodeException {
    String template = "{a|AbsUrl}";
    String json = "{\"base-url\": \"http://foobar.com/foo\", \"a\": \"abc\"}";
    Instruction code = compiler().compile(template).getCode();
    String result = eval(compiler().execute(code, JSONUtils.decode(json)));
    assertEquals(result, "http://foobar.com/foo/abc");
  }
  
  @Test
  public void testApplyPartial() throws CodeException {
    String partials = "{\"block.item\": \"this {@} value\"}";
    String input = "{\"foo\": 123}";
    
    CodeMaker mk = maker();
    CodeBuilder cb = builder().text("hi ");
    cb.formatter("foo", CoreFormatters.APPLY, mk.args(" block.item"));
    Instruction root = cb.text("!").eof().build();

    Context ctx = new Context(JSONUtils.decode(input));
    ctx.setCompiler(compiler());
    ctx.setPartials(JSONUtils.decode(partials));
    ctx.execute(root);
    assertContext(ctx, "hi this 123 value!");
  }
  
  @Test
  public void testApplyPartialError() throws CodeException {
    String template = "{@|apply block}";
    String partials = "{\"block\": \"{.section foo}{@}\"}";
    String input = "{\"foo\": 123}";
    Instruction inst = compiler().compile(template).getCode();
    Context ctx = new Context(JSONUtils.decode(input));
    ctx.setCompiler(compiler());
    ctx.setPartials(JSONUtils.decode(partials));
    try {
      ctx.execute(inst);
      fail("Expected exception.");
    } catch (CodeExecuteException e) {
      assertEquals(e.getErrorInfo().getType(), APPLY_PARTIAL_SYNTAX);
    }
  }
  
  @Test
  public void testCount() throws CodeException {
    for (String val : new String[] { "null", "0", "\"foo\"" }) {
      assertFormatter(CoreFormatters.COUNT, val, "0");
    }
    assertFormatter(CoreFormatters.COUNT, "[1,2,3]", "3");
    assertFormatter(CoreFormatters.COUNT, "{\"a\": 1, \"b\": 2}", "2");
  }
  
  @Test
  public void testCycle() throws CodeException {
    CodeMaker mk = maker();     
    Arguments args = mk.args(" A B C");
    String result = "";
    for (int i = -6; i < 6; i++) {
      result += format(CoreFormatters.CYCLE, args, Integer.toString(i));
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

    String r1 = formatDate("%a %d %b %Y %H:%M:%S %p %Z", NOV_15_2013_123030_UTC, tzNewYork);
    String r2 = formatDate("%c", NOV_15_2013_123030_UTC, tzNewYork);
    assertEquals(r1, r2);

    r1 = formatDate("%F", NOV_15_2013_123030_UTC, tzLosAngeles);
    r2 = formatDate("%Y-%m-%d", NOV_15_2013_123030_UTC, tzLosAngeles);
    assertEquals(r1, r2);
    
    r1 = formatDate("%D", NOV_15_2013_123030_UTC, tzNewYork);
    r2 = formatDate("%m/%d/%y", NOV_15_2013_123030_UTC, tzNewYork);
    assertEquals(r1, r2);
    
    r1 = formatDate("%R", NOV_15_2013_123030_UTC, tzNewYork);
    r2 = formatDate("%H:%M", NOV_15_2013_123030_UTC, tzNewYork);
    assertEquals(r1, r2);
    
    r1 = formatDate("%X", NOV_15_2013_123030_UTC, tzNewYork);
    r2 = formatDate("%H:%M:%S %p", NOV_15_2013_123030_UTC, tzNewYork);
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
  
  private String formatDate(String format, long timestamp, String tzId) throws CodeException {
    String template = "{time|date " + format + "}";
    String json = getDateTestJson(timestamp, tzId);
    Instruction code = compiler().compile(template).getCode();
    return eval(compiler().execute(code, JSONUtils.decode(json)));
  }
  
 
  @Test
  public void testEncodeSpace() throws CodeException {
    assertFormatter(ENCODE_SPACE, "\"  \\n \"", "&nbsp;&nbsp;&nbsp;&nbsp;");
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
    assertFormatter(JSON, "\"foo </script>\"", "\"foo </scr\"+\"ipt>\"");

    assertFormatter(JSON_PRETTY, "{ \"a\":  3.14159 }", "{\n  \"a\" : 3.14159\n}");
  }
  
  @Test
  public void testOutput() throws CodeException {
    CodeMaker mk = maker();
    Arguments args = mk.args(":1:2:3");
    assertFormatter(CoreFormatters.OUTPUT, args, "{}", "1 2 3");
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
  public void testSafe() throws CodeException {
    assertFormatter(CoreFormatters.SAFE, "\"foo <bar> bar\"", "foo  bar");
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
    assertFormatter(CoreFormatters.SMARTYPANTS, "\"Fred's\"", "Fred\u2019s");
    assertFormatter(CoreFormatters.SMARTYPANTS, "\"\\\"foo\\\"\"", "\u201cfoo\u201d");
  }
  
  @Test
  public void testStr() throws CodeException {
    for (String json : new String[] { "123", "3.14159", "-12", "false", "true" }) {
      assertFormatter(CoreFormatters.STR, json, json);
    }
    assertFormatter(CoreFormatters.STR, "\"abc\"", "abc");
    assertFormatter(CoreFormatters.STR, "null", "");
  }
  
  @Test
  public void testTimeSince() throws CodeException {
    String now = Long.toString(System.currentTimeMillis() - (1000 * 15));
    String result = format(TIMESINCE, now);
    assertTrue(result.contains("less than a minute ago"));
  }
  
  @Test
  public void testTruncate() throws CodeException {
    CodeMaker mk = maker();
    assertFormatter(TRUNCATE, mk.args(" 5 .."), "\"foo bar baz\"", "foo ..");
  }
  
  @Test
  public void testUrlEncode() throws CodeException {
    assertFormatter(CoreFormatters.URL_ENCODE, "\"\u201ca b\u201d\"", "%E2%80%9Ca+b%E2%80%9D");
  }
  
  
  protected static String getDateTestJson(long timestamp, String tzId) {
    DateTimeZone timezone = DateTimeZone.forID(tzId);
    ObjectNode node = JSONUtils.createObjectNode();
    node.put("time", timestamp);
    ObjectNode website = JSONUtils.createObjectNode();
    website.put("timeZoneOffset", timezone.getOffset(timestamp));
    website.put("timeZone", timezone.getID());
    node.put("website", website);
    return node.toString();
  }
}
