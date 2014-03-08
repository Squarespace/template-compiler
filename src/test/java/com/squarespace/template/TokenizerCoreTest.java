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
import static com.squarespace.template.Operator.LOGICAL_OR;
import static com.squarespace.template.SyntaxErrorType.EOF_IN_COMMENT;
import static com.squarespace.template.SyntaxErrorType.EXTRA_CHARS;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_ARGS_INVALID;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_INVALID;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_NEEDS_ARGS;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_UNKNOWN;
import static com.squarespace.template.SyntaxErrorType.IF_EMPTY;
import static com.squarespace.template.SyntaxErrorType.IF_EXPECTED_VAROP;
import static com.squarespace.template.SyntaxErrorType.IF_TOO_MANY_OPERATORS;
import static com.squarespace.template.SyntaxErrorType.IF_TOO_MANY_VARS;
import static com.squarespace.template.SyntaxErrorType.INVALID_INSTRUCTION;
import static com.squarespace.template.SyntaxErrorType.MISSING_SECTION_KEYWORD;
import static com.squarespace.template.SyntaxErrorType.MISSING_WITH_KEYWORD;
import static com.squarespace.template.SyntaxErrorType.OR_EXPECTED_PREDICATE;
import static com.squarespace.template.SyntaxErrorType.PREDICATE_ARGS_INVALID;
import static com.squarespace.template.SyntaxErrorType.PREDICATE_UNKNOWN;
import static com.squarespace.template.SyntaxErrorType.WHITESPACE_EXPECTED;
import static com.squarespace.template.plugins.CorePredicates.PLURAL;
import static com.squarespace.template.plugins.CorePredicates.SINGULAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.template.plugins.CoreFormatters;


/**
 * Tokenizer validation tests. Ensures that the parsed strings result in the correct
 * sequences of instructions, or that the expected error is raised.
 */
@Test(groups = { "unit" })
public class TokenizerCoreTest extends UnitTestBase {

  private static final boolean VERBOSE = false;

  @Test
  public void testEdgeCases() throws CodeSyntaxException {
    CodeMaker mk = maker();
    assertResult("", mk.eof());
    assertResult(" ", mk.text(" "), mk.eof());
    assertResult("}", mk.text("}"), mk.eof());
    assertResult("{", mk.text("{"), mk.eof());
    assertResult("\n}", mk.text("\n}"), mk.eof());
    assertResult("\n{", mk.text("\n{"), mk.eof());
    assertResult(" {}", mk.text(" "), mk.text("{}"), mk.eof());

    // Try some ambiguous inputs
    assertResult("{ foo {{}", mk.text("{ foo {"), mk.text("{}"), mk.eof());
    assertResult("{/x}", mk.text("{/x}"), mk.eof());

    assertResult("{#", mk.text("{#"), mk.eof());

    // Variable references without formatters must have zero trailing chars, including whitespace.
    // This lets us better handle passing through Javascript in the template, for example:
    // <script>function foo(){bar+=1;}</script>

    assertResult("{foo.baz }", mk.text("{foo.baz }"), mk.eof());
    assertResult("{a.b=1;return 2;}", mk.text("{a.b=1;return 2;}"), mk.eof());
    assertResult("{return a.b}", mk.text("{return a.b}"), mk.eof());

    // There are limits to how much ambiguity we can tolerate. The PIPE causes this to expect
    // a Formatter identifier to follow, and its harder to discern the intent.
    assertFailure("{a|=3;return a;}", "javascript-y text");
  }

  @Test
  public void testAlternatesWith() throws CodeSyntaxException {
    CodeMaker mk = maker();
    assertResult("{.alternates with}", mk.alternates(), mk.eof());
    assertResult("{.alternates with}-{.end}", mk.alternates(), mk.text("-"), mk.end(), mk.eof());

    assertFailure("{.alternates=with}", WHITESPACE_EXPECTED);
    assertFailure("{.alternates wxth}", MISSING_WITH_KEYWORD);
    assertFailure("{.alternates \t with}", MISSING_WITH_KEYWORD);
    assertFailure("{.alternate with}", INVALID_INSTRUCTION);
    assertFailure("{.alternates with xyz}", EXTRA_CHARS);
    assertFailure("{.alternates with\t}", EXTRA_CHARS);
  }

  @Test
  public void testComments() throws CodeSyntaxException {
    CodeMaker mk = maker();
    assertResult("{# foo bar}", mk.comment(" foo bar"), mk.eof());
    assertResult("{# ##foo}", mk.comment(" ##foo"), mk.eof());
    assertResult("{ # foo bar}", mk.text("{ # foo bar}"), mk.eof());

    assertResult("{## foo ##}", mk.mcomment(" foo "), mk.eof());
    assertResult("{##\n##{}##\n##}", mk.mcomment("\n##{}##\n"), mk.eof());
    assertResult("{#######}", mk.mcomment("###"), mk.eof());
    assertResult("{##\n##}", mk.mcomment("\n"), mk.eof());

    assertFailure("{## foo ", EOF_IN_COMMENT);
    assertFailure("{## foo }", EOF_IN_COMMENT);
  }

  @Test
  public void testEnd() throws CodeSyntaxException {
    CodeMaker mk = maker();
    assertResult("{.end}", mk.end(), mk.eof());
    assertResult("{ .end}", mk.text("{ .end}"), mk.eof());

    assertFailure("{.end xyz}", EXTRA_CHARS);
    assertFailure("{.end \t }", EXTRA_CHARS);
    assertFailure("{.endx}", INVALID_INSTRUCTION);
  }

  @Test
  public void testFormatter() throws CodeSyntaxException {
    CodeMaker mk = maker();
    Arguments args1 = mk.args(" a1 a2");
    assertResult("{@|pluralize}", mk.var("@", CoreFormatters.PLURALIZE), mk.eof());
    assertResult("{a.b.c|slugify}", mk.var("a.b.c", CoreFormatters.SLUGIFY), mk.eof());
    assertResult("{foo|pluralize a1 a2}", mk.var("foo", mk.fmt(CoreFormatters.PLURALIZE, args1)), mk.eof());

    Arguments args2 = mk.args("/b/c");
    assertResult("{a|pluralize/b/c}", mk.var("a", mk.fmt(CoreFormatters.PLURALIZE, args2)), mk.eof());
  }

  @Test
  public void testFormatterErrors() throws CodeSyntaxException {
    // Too many arguments.
    assertFailure("{a|pluralize/a/b/c}", FORMATTER_ARGS_INVALID);

    // Argument is invalid
    assertFailure("{foo|invalid-args bar}", FORMATTER_ARGS_INVALID);

    assertFailure("{foo|plrlize}", FORMATTER_UNKNOWN);
    assertFailure("{foo|do-stuff}", FORMATTER_UNKNOWN);

    assertFailure("{foo|=123}", FORMATTER_INVALID);
    assertFailure("{foo|}", FORMATTER_INVALID);
    assertFailure("{foo|123}", FORMATTER_INVALID);

    // Formatter arguments are required
    assertFailure("{foo|invalid-args a b}", FORMATTER_ARGS_INVALID);
    assertFailure("{foo|required-args}", FORMATTER_NEEDS_ARGS);
    assertFailure("{foo|apply}", FORMATTER_NEEDS_ARGS);
  }

  @Test
  public void testIf() throws CodeSyntaxException {
    CodeMaker mk = maker();
    assertResult("{.if a}", mk.ifexpn(mk.strlist("a"), mk.oplist()), mk.eof());
    assertResult("{.if a||b}", mk.ifexpn(mk.strlist("a", "b"), mk.oplist(LOGICAL_OR)), mk.eof());
    assertResult("{.if a.b && c.d}", mk.ifexpn(mk.strlist("a.b", "c.d"), mk.oplist(LOGICAL_AND)), mk.eof());
    assertResult("{.if a&&b||c}", mk.ifexpn(mk.strlist("a", "b", "c"), mk.oplist(LOGICAL_AND, LOGICAL_OR)), mk.eof());

    List<String> vars = mk.strlist("a", "b", "c", "d", "e");
    List<Operator> ops = mk.oplist(LOGICAL_OR, LOGICAL_OR, LOGICAL_OR, LOGICAL_OR);
    assertResult("{.if a||b||c||d||e}", mk.ifexpn(vars, ops), mk.eof());

    assertFailure("{.if}", WHITESPACE_EXPECTED);
    assertFailure("{.if }", IF_EMPTY);
    assertFailure("{.if a||b||c||d||e||}", IF_TOO_MANY_OPERATORS);
    assertFailure("{.if a||b||c||d||e||f}", IF_TOO_MANY_VARS);
    assertFailure("{.if .qrs||.tuv}", IF_EXPECTED_VAROP);
  }

  @Test
  public void testLiteral() throws CodeSyntaxException {
    CodeMaker mk = maker();
    assertResult("{.space}", mk.space(), mk.eof());
    assertResult("{.tab}", mk.tab(), mk.eof());
    assertResult("{.newline}", mk.newline(), mk.eof());
  }

  @Test
  public void testMeta() throws CodeSyntaxException {
    CodeMaker mk = maker();
    assertResult("{.meta-left}", mk.metaLeft(), mk.eof());
    assertResult("{.meta-right}", mk.metaRight(), mk.eof());

    assertFailure("{.meta-right   }", EXTRA_CHARS);
  }

  @Test
  public void testPredicate() throws CodeSyntaxException {
    CodeMaker mk = maker();
    assertResult("{.plural?}", mk.predicate(PLURAL), mk.eof());
    assertResult("{.plural?/a/b}", mk.predicate(PLURAL, mk.args("/a/b")), mk.eof());
    assertResult("{.plural? a b}", mk.predicate(PLURAL, mk.args(" a b")), mk.eof());
    assertResult("{.singular?}", mk.predicate(SINGULAR), mk.eof());
    assertResult("{.or}", mk.or(), mk.eof());
    assertResult("{.or plural?}", mk.or(PLURAL), mk.eof());

    assertFailure("{.or=}", EXTRA_CHARS);
    assertFailure("{.plrul?}", PREDICATE_UNKNOWN);
    assertFailure("{.or foo?}", PREDICATE_UNKNOWN);
    assertFailure("{.or foo}", OR_EXPECTED_PREDICATE);
    assertFailure("{.or foo foo}", OR_EXPECTED_PREDICATE);
    assertFailure("{.or .foo?}", OR_EXPECTED_PREDICATE);

    assertFailure("{.or invalid-args? a b}", PREDICATE_ARGS_INVALID);
  }

  @Test
  public void testSection() throws CodeSyntaxException {
    CodeMaker mk = maker();
    assertResult("{.section @}", mk.section("@"), mk.eof());
    assertResult("{.section 1}", mk.section("1"), mk.eof());

    assertFailure("{.section}", WHITESPACE_EXPECTED);
  }

  @Test
  public void testRepeat() throws CodeSyntaxException {
    CodeMaker mk = maker();
    assertResult("{.repeated section @}", mk.repeated("@"), mk.eof());
    assertResult("{.repeated section a.b.c}", mk.repeated("a.b.c"), mk.eof());
    assertResult("{.repeated section a}A{.end}", mk.repeated("a"), mk.text("A"), mk.end(), mk.eof());

    // Invalid instruction but parses as TEXT
    assertResult("{  .repeated section x}", mk.text("{  .repeated section x}"), mk.eof());

    // sections, et al, can index into arrays.
    assertResult("{.repeated section 123}", mk.repeated("123"), mk.eof());

    assertFailure("{.repeated=}", WHITESPACE_EXPECTED);
    assertFailure("{.repeated = a}", MISSING_SECTION_KEYWORD);
    assertFailure("{.repeated section}", WHITESPACE_EXPECTED);
    assertFailure("{.repeated section abc xyz}", EXTRA_CHARS);
  }

  @Test
  public void testVariable() throws CodeSyntaxException {
    CodeMaker mk = maker();
    assertResult("{@}", mk.var("@"), mk.eof());
    assertResult("{@index}", mk.var("@index"), mk.eof());
    assertResult("{foo.bar}", mk.var("foo.bar"), mk.eof());
  }

  private void assertFailure(String raw, SyntaxErrorType type) {
    try {
      tokenizer(raw).consume();
      fail("expected SyntaxError but none thrown");
    } catch (CodeSyntaxException e) {
      if (VERBOSE) {
        System.out.println("assertFailure: " + e.getMessage());
      }
      assertNotEquals(e.getErrorInfo(), null, "ErrorInfo is null");
      assertEquals(e.getErrorInfo().getType(), type, "error type doesn't match");
      assertEquals(e.getMessage().indexOf(Constants.NULL_PLACEHOLDER), -1, "found null placeholder");
    }
  }

  private void assertFailure(String raw, String msg) {
    try {
      tokenizer(raw).consume();
      fail("tokenizer fail: " + msg);
    } catch (CodeSyntaxException e) {
      if (VERBOSE) {
        System.out.println("assertFailure: " + e.getMessage());
      }
    }
  }

  private void assertResult(String raw, Instruction ... instructions) throws CodeSyntaxException {
    CodeList collector = collector();
    tokenizer(raw, collector).consume();
    List<Instruction> expected = Arrays.asList(instructions);
    if (VERBOSE) {
      System.out.println("assertResult: " + collector.getInstructions());
    }
    assertEquals(collector.getInstructions(), expected);
  }

}
