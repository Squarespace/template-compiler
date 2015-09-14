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

import static com.squarespace.template.Operator.LOGICAL_AND;
import static com.squarespace.template.SyntaxErrorType.DEAD_CODE_BLOCK;
import static com.squarespace.template.SyntaxErrorType.EOF_IN_BLOCK;
import static com.squarespace.template.SyntaxErrorType.NOT_ALLOWED_AT_ROOT;
import static com.squarespace.template.SyntaxErrorType.NOT_ALLOWED_IN_BLOCK;
import static com.squarespace.template.plugins.CorePredicates.EQUAL;
import static com.squarespace.template.plugins.CorePredicates.PLURAL;
import static com.squarespace.template.plugins.CorePredicates.SINGULAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.template.Instructions.RootInst;
import com.squarespace.template.plugins.CoreFormatters.SafeFormatter;
import com.squarespace.template.plugins.CoreFormatters.TruncateFormatter;
import com.squarespace.template.plugins.CorePredicates;


/**
 * This test case ensures that valid sequences are accepted and executed by the
 * CodeBuilder, producing the expected result, and invalid sequences are rejected
 * and raise the expected error.
 */
@Test(groups = { "unit" })
public class CodeValidityTest extends UnitTestBase {

  private static final Formatter SAFE = new SafeFormatter();

  private static final Formatter TRUNCATE = new TruncateFormatter();

  /**
   * Sort of confusing to add sections to ALTERNATES_WITH block, since we're always pointing to the
   * next element in the array when that block is executed. Typical uses of ALTERNATES_WITH just output
   * static text, but nesting things inside those blocks is possible.
   */
  @Test
  public void testAlternatesWith() throws CodeException {
    RootInst root = builder().repeated("a").alternatesWith().section("b").var("@").end().end().eof().build();
    String json = "{\"a\": [{\"b\": 1}, {\"b\": 2}, {\"b\": 3}, {\"b\": 4}]}";
    assertContext(execute(json, root), "123");
  }

  @Test
  public void testAlternatesWithSectionOr() throws CodeException {
    CodeBuilder cb = builder().repeated("a").alternatesWith().section("@").var("@").end();
    RootInst root = cb.or().text("x").end().eof().build();
    assertContext(execute("{\"a\": [1,2,3]}", root), "12");
    assertContext(execute("{}", root), "x");
  }

  @Test
  public void testAlternatesWithOr() throws CodeException {
    RootInst root = builder().repeated("a").text("A").alternatesWith().text("-").or().text("B").end().eof().build();
    assertContext(execute("{\"a\": [1,2,3]}", root), "A-A-A");
    assertContext(execute("{}", root), "B");
  }

  @Test
  public void testComments() throws CodeException {
    RootInst root = builder().comment("foo").mcomment("bar\nbaz").eof().build();
    assertContext(execute("{}", root), "");
  }

  @Test
  public void testIfExpression() throws CodeException {
    CodeMaker mk = maker();

    // AND TESTS

    CodeBuilder cb = builder().ifexpn(mk.strlist("a", "b"), mk.oplist(Operator.LOGICAL_AND));
    RootInst root = cb.text("A").or().text("B").end().eof().build();

    assertContext(execute("{\"a\": 1, \"b\": 3.14159}", root), "A");
    assertContext(execute("{\"a\": true, \"b\": \"Bill\"}", root), "A");

    assertContext(execute("{\"a\": 1}", root), "B");
    assertContext(execute("{\"a\": true, \"b\": false}", root), "B");
    assertContext(execute("{}", root), "B");

    // Index into nested arrays.
    cb = builder().ifexpn(mk.strlist("a.0.b", "a.1.b"), mk.oplist(Operator.LOGICAL_AND));
    root = cb.text("A").or().text("B").end().eof().build();
    assertContext(execute("{\"a\": [{\"b\": 1}, {\"b\": true}]}", root), "A");
    assertContext(execute("{\"a\": [{\"b\": 3.14}, {\"b\": \"hi\"}]}", root), "A");
    assertContext(execute("{\"a\": [{\"b\": null}, {\"b\": true}]}", root), "B");
    assertContext(execute("{\"a\": [{\"b\": \"\"}, {\"b\": true}]}", root), "B");

    root = builder().ifexpn(mk.strlist("a"), mk.oplist()).text("A").end().eof().build();
    assertContext(execute("{\"a\": 1}", root), "A");
    assertContext(execute("{}", root), "");

    // OR TESTS

    cb = builder().ifexpn(mk.strlist("a", "b", "c"), mk.oplist(Operator.LOGICAL_OR, Operator.LOGICAL_OR));
    root = cb.text("A").or().text("B").end().eof().build();

    assertContext(execute("{\"a\": true}", root), "A");
    assertContext(execute("{\"b\": \"Bill\"}", root), "A");
    assertContext(execute("{\"c\": 3.14159}", root), "A");
    assertContext(execute("{\"a\": false, \"b\": 0, \"c\": \"Fred\"}", root), "A");
    assertContext(execute("{\"a\": {}, \"b\": {\"c\": 1}}", root), "A");

    assertContext(execute("{}", root), "B");
    assertContext(execute("{\"a\": \"\"}", root), "B");
    assertContext(execute("{\"a\": null}", root), "B");
    assertContext(execute("{\"c\": false}", root), "B");
    assertContext(execute("{\"c\": false, \"b\": 0}", root), "B");
  }

  @Test
  public void testIfExpressionVars() throws CodeException {
    CodeMaker mk = maker();
    // Nested variables
    CodeBuilder cb = builder().ifexpn(mk.strlist("a.b", "a.c"), mk.oplist(Operator.LOGICAL_OR));
    Instruction root = cb.text("A").or().text("B").end().eof().build();

    // True cases
    assertContext(execute("{\"a\": {\"b\": 1}}", root), "A");
    assertContext(execute("{\"a\": {\"b\": 0, \"c\": true}}", root), "A");
    assertContext(execute("{\"a\": {\"c\": 1}}", root), "A");

    // False cases
    assertContext(execute("{\"a\": {}}", root), "B");
    assertContext(execute("{\"a\": {\"b\": 0}}", root), "B");
    assertContext(execute("{\"a\": {\"b\": 0, \"c\": 0}}", root), "B");
    assertContext(execute("{\"a\": {\"c\": 0}}", root), "B");
  }

  @Test
  public void testIfExpressionScope() throws CodeException {
    CodeMaker mk = maker();
    Instruction root = builder().ifexpn(mk.strlist("a"), mk.oplist()).or().var("b").end().eof().build();
    // True cases
    assertContext(execute("{\"a\": 1}", root), "");
    assertContext(execute("{\"a\": 1, \"b\": \"B\"}", root), "");

    // False cases
    assertContext(execute("{\"a\": 0}", root), "");
    assertContext(execute("{\"a\": 0, \"b\": \"B\"}", root), "B");
  }

  @Test
  public void testIfPredicate() throws CodeException {
    CodeMaker mk = maker();
    RootInst root = builder().ifpred(PLURAL).text("A").or(SINGULAR).text("B").or().text("C").end().eof().build();
    assertContext(execute("5", root), "A");
    assertContext(execute("1", root), "B");
    assertContext(execute("0", root), "C");

    root = builder().ifpred(EQUAL, mk.args(" 2")).text("A").or().text("B").end().eof().build();
    assertContext(execute("2", root), "A");
    assertContext(execute("1", root), "B");
  }

  @Test
  public void testPredicate() throws CodeException {
    CodeBuilder cb = builder().section("@").predicate(PLURAL).text("A");
    cb.or(CorePredicates.SINGULAR).text("B");
    cb.or().text("C").end(); // end or
    cb.end(); // section

    RootInst root = cb.eof().build();
    assertEquals(repr(root), "{.section @}{.plural?}A{.or singular?}B{.or}C{.end}{.end}");

    assertContext(execute("174", root), "A");
    assertContext(execute("174.35", root), "A");
    assertContext(execute("1", root), "B");
    assertContext(execute("1.0", root), "B");

    // zero is false-y, so entire section is skipped
    assertContext(execute("0", root), "");
  }

  @Test
  public void testOrWithSection() throws CodeException {
    // This construction makes almost no sense but is possible in the language,
    // so adding it for coverage.
    RootInst root = builder().section("@").or().predicate(PLURAL).or().text("A").var("@").end().end().eof().build();
    assertEquals(repr(root), "{.section @}{.or}{.plural?}{.or}A{@}{.end}{.end}");
    assertContext(execute("0", root), "A0");
  }

  @Test
  public void testPredicateInvalid() throws CodeException {
    CodeMaker mk = maker();
    assertInvalid(DEAD_CODE_BLOCK, mk.predicate(PLURAL), mk.text("A"), mk.or(), mk.text("B"), mk.or());
    assertInvalid(EOF_IN_BLOCK, mk.predicate(PLURAL), mk.eof());
  }

  @Test
  public void testRepeated() throws CodeException {
    String jsonData = "{\"foo\": [0, 0, 0]}";
    RootInst root = builder().repeated("foo").text("1").var("@").alternatesWith().text("-").end().eof().build();
    assertContext(execute(jsonData, root), "10-10-10");

    root = builder().repeated("bar").text("1").end().eof().build();
    assertContext(execute("{}", root), "");
  }

  @Test
  public void testRepeatedOr() throws CodeException {
    String jsonData = "{\"a\": [0, 0, 0]}";
    RootInst root = builder().repeated("a").var("@").alternatesWith().text("-").or().text("X").end().eof().build();
    assertContext(execute(jsonData, root), "0-0-0");

    jsonData = "{\"b\": [0, 0, 0]}";
    assertContext(execute(jsonData, root), "X");
  }

  @Test
  public void testRepeatedIndex() throws CodeException {
    String jsonData = "{\"foo\": [\"A\", \"B\", \"C\"]}";
    CodeBuilder cb = builder();
    cb.repeated("foo").var("@").var("@index").alternatesWith().text(".").end().eof();
    RootInst root = cb.build();
    // @index is 1-based
    assertContext(execute(jsonData, root), "A1.B2.C3");

    jsonData = "{\"a\": [\"x\", \"y\"]}";
    cb = builder();
    cb.repeated("a").var("@").section("@").var("@").var("@index").end().alternatesWith().text(".").end().eof();
    assertContext(execute(jsonData, cb.build()), "xx1.yy2");
  }

  @Test
  public void testRepeatedScope() throws CodeException {
    RootInst root = builder().repeated("a").var("@index").or().var("b").end().eof().build();
    assertContext(execute("{\"a\": [0, 0], \"b\": \"B\"}", root), "12");
    assertContext(execute("{\"a\": null, \"b\": \"B\"}", root), "B");
  }

  @Test
  public void testSection() throws CodeException {
    RootInst root = builder().section("foo").var("bar").or().text("B").end().eof().build();
    assertContext(execute("{\"foo\": 1}", root), "");
    assertContext(execute("{}", root), "B");
    assertContext(execute("{\"foo\": {\"bar\": 1}}", root), "1");
  }

  @Test
  public void testSectionScope() throws CodeException {
    RootInst root = builder().section("a").var("a").or().var("b").end().eof().build();
    assertContext(execute("{\"a\": 1, \"b\": \"B\"}", root), "1");
    assertContext(execute("{\"a\": 0, \"b\": \"B\"}", root), "B");
  }

  @Test
  public void testText() throws CodeException  {
    RootInst root = builder().text("foo").text("bar").eof().build();
    assertContext(execute("{}", root), "foobar");

    root = builder().eof().build();
    assertContext(execute("{}", root), "");
  }

  @Test
  public void testVariables() throws CodeException {
    RootInst root = builder().var("foo").var("bar").eof().build();
    assertContext(execute("{\"foo\": 1, \"bar\": 2}", root), "12");
  }

  @Test
  public void testVariableFormatters() throws CodeException {
    RootInst root = builder().var("a", SAFE).eof().build();
    assertContext(execute("{\"a\": \"x <> y\"}", root), "x  y");

    // Chain formatters together.
    CodeMaker mk = maker();
    List<FormatterCall> formatters = mk.formatters(mk.fmt(SAFE), mk.fmt(TRUNCATE, mk.args(" 5")));
    root = builder().var("a", formatters).eof().build();
    assertContext(execute("{\"a\": \"x <> y <> z\"}", root), "x  y ...");
  }

  @Test
  public void testDeadCode() {
    CodeMaker mk = maker();
    assertInvalid(DEAD_CODE_BLOCK, mk.predicate(PLURAL), mk.text("A"), mk.or(), mk.text("B"), mk.or());
    assertInvalid(DEAD_CODE_BLOCK, mk.repeated("@"), mk.text("A"), mk.alternates(), mk.or(), mk.or());
  }

  @Test
  public void testEOFInBlock() throws ArgumentsException {
    CodeMaker mk = maker();
    assertInvalid(EOF_IN_BLOCK, mk.section("@"), mk.eof());
    assertInvalid(EOF_IN_BLOCK, mk.section("@"), mk.section("a"), mk.eof());
    assertInvalid(EOF_IN_BLOCK, mk.repeated("@"), mk.eof());
    assertInvalid(EOF_IN_BLOCK, mk.repeated("@"), mk.repeated("b"), mk.eof());
    assertInvalid(EOF_IN_BLOCK, mk.ifexpn(mk.strlist("a", "b"), mk.oplist(LOGICAL_AND)), mk.eof());
    assertInvalid(EOF_IN_BLOCK, mk.ifpred(PLURAL), mk.or(), mk.eof());
    assertInvalid(EOF_IN_BLOCK, mk.predicate(PLURAL), mk.eof());
    assertInvalid(EOF_IN_BLOCK, mk.section("@"), mk.or(), mk.eof());
    assertInvalid(EOF_IN_BLOCK, mk.section("a"), mk.or(PLURAL), mk.eof());
    assertInvalid(EOF_IN_BLOCK, mk.repeated("@"), mk.alternates(), mk.eof());
  }

  @Test
  public void testUnexpectedInstructions() {
    CodeMaker mk = maker();
    assertInvalid(NOT_ALLOWED_AT_ROOT, mk.or());
    assertInvalid(NOT_ALLOWED_AT_ROOT, mk.or(SINGULAR));
    assertInvalid(NOT_ALLOWED_AT_ROOT, mk.alternates());

    assertInvalid(NOT_ALLOWED_IN_BLOCK, mk.repeated("@"), mk.or(), mk.alternates());
    assertInvalid(NOT_ALLOWED_IN_BLOCK, mk.section("a"), mk.or(), mk.alternates());
    assertInvalid(NOT_ALLOWED_IN_BLOCK, mk.predicate(PLURAL), mk.alternates());
    assertInvalid(NOT_ALLOWED_IN_BLOCK, mk.repeated("@"), mk.alternates(), mk.alternates());
  }

  private void assertInvalid(SyntaxErrorType type, Instruction... instructions) {
    try {
      CodeBuilder cb = builder();
      for (Instruction inst : instructions) {
        cb.accept(inst);
      }
      cb.build();
      fail(type + " should have raised a syntax exception");

    } catch (CodeSyntaxException e) {
      // Exception means success.
      assertEquals(e.getErrorInfo().getType(), type);
    }
  }

}
