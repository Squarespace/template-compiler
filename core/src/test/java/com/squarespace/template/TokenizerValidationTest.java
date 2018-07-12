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

import static com.squarespace.template.SyntaxErrorType.EOF_IN_COMMENT;
import static com.squarespace.template.SyntaxErrorType.EXTRA_CHARS;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_ARGS_INVALID;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_INVALID;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_NEEDS_ARGS;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_UNKNOWN;
import static com.squarespace.template.SyntaxErrorType.IF_EMPTY;
import static com.squarespace.template.SyntaxErrorType.IF_EXPECTED_VAROP;
import static com.squarespace.template.SyntaxErrorType.IF_TOO_MANY_OPERATORS;
import static com.squarespace.template.SyntaxErrorType.INVALID_INSTRUCTION;
import static com.squarespace.template.SyntaxErrorType.MISSING_SECTION_KEYWORD;
import static com.squarespace.template.SyntaxErrorType.MISSING_WITH_KEYWORD;
import static com.squarespace.template.SyntaxErrorType.OR_EXPECTED_PREDICATE;
import static com.squarespace.template.SyntaxErrorType.PREDICATE_ARGS_INVALID;
import static com.squarespace.template.SyntaxErrorType.PREDICATE_NEEDS_ARGS;
import static com.squarespace.template.SyntaxErrorType.PREDICATE_UNKNOWN;
import static com.squarespace.template.SyntaxErrorType.VARIABLE_EXPECTED;
import static com.squarespace.template.SyntaxErrorType.WHITESPACE_EXPECTED;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;


/**
 * Tests the Tokenizer's validation mode, where it continues parsing after
 * encountering an error.  When hooked to a CodeMachine more errors would
 * be reported, but these tests only verify errors raised by the Tokenizer
 * class.
 */
@Test(groups = { "unit" })
public class TokenizerValidationTest extends UnitTestBase {

  private static final boolean VERBOSE = false;

  @Test
  public void testAlternatesWith() throws CodeSyntaxException {
    assertErrors("{.alternates}", WHITESPACE_EXPECTED);
    assertErrors("{.alternates x}", MISSING_WITH_KEYWORD);
    assertErrors("{.alternates with }", EXTRA_CHARS);
  }

  @Test
  public void testComments() throws CodeSyntaxException {
    assertErrors("{##\nfoo #", EOF_IN_COMMENT);
  }

  @Test
  public void testIfExpression() throws CodeSyntaxException {

    // Boolean expressions

    assertErrors("{.i}", INVALID_INSTRUCTION);
    assertErrors("{.if}", WHITESPACE_EXPECTED);
    assertErrors("{.if }", IF_EMPTY);
    assertErrors("{.if ?}", IF_EXPECTED_VAROP);
    assertErrors("{.if a ||}", IF_TOO_MANY_OPERATORS);
    assertErrors("{.if a || b ||}", IF_TOO_MANY_OPERATORS);
    assertErrors("{.if a b}", IF_EXPECTED_VAROP);
    assertErrors("{.if a .}", IF_EXPECTED_VAROP);
    assertErrors("{.if " + StringUtils.repeat("a || ", 30) + "a" + " }", SyntaxErrorType.IF_TOO_MANY_VARS);

    // Predicates
    assertErrors("{.if foo?}", PREDICATE_UNKNOWN);
  }

  @Test
  public void testOrPredicate() throws CodeSyntaxException {
    assertErrors("{.or.}", EXTRA_CHARS);
    assertErrors("{.or }", OR_EXPECTED_PREDICATE);
    assertErrors("{.or .}", OR_EXPECTED_PREDICATE);
    assertErrors("{.or foo?}", PREDICATE_UNKNOWN);
    assertErrors("{.or required-args?}", PREDICATE_NEEDS_ARGS);
    assertErrors("{.or invalid-args?}", PREDICATE_ARGS_INVALID);
  }

  @Test
  public void testRepeated() throws CodeSyntaxException {
    assertErrors("{.repeated}", WHITESPACE_EXPECTED);
    assertErrors("{.repeated.}", WHITESPACE_EXPECTED);
    assertErrors("{.repeated sekshun}", MISSING_SECTION_KEYWORD);
    assertErrors("{.repeated section}", WHITESPACE_EXPECTED);
    assertErrors("{.repeated section.}", WHITESPACE_EXPECTED);
    assertErrors("{.repeated section a }", EXTRA_CHARS);
  }

  @Test
  public void testSection() throws CodeSyntaxException {
    assertErrors("{.section}", WHITESPACE_EXPECTED);
    assertErrors("{.section.}", WHITESPACE_EXPECTED);
    assertErrors("{.section ?}", VARIABLE_EXPECTED);
    assertErrors("{.section a }", EXTRA_CHARS);
  }

  @Test
  public void testTerminal() throws CodeSyntaxException {
    assertErrors("{.endx}", INVALID_INSTRUCTION);
    assertErrors("{.end }", EXTRA_CHARS);
    assertErrors("{.end end}", EXTRA_CHARS);
  }

  @Test
  public void testVariables() throws CodeSyntaxException {
    assertErrors("{a|?}", FORMATTER_INVALID);
    assertErrors("{a|foo}", FORMATTER_UNKNOWN);
    assertErrors("{a|required-args}", FORMATTER_NEEDS_ARGS);
    assertErrors("{a|invalid-args}", FORMATTER_ARGS_INVALID);
  }

  @Test
  public void testVariableFormatters() throws CodeSyntaxException {
    assertErrors("{a|safe|safe|required-args}", FORMATTER_NEEDS_ARGS);
    assertErrors("{a|safe|safe|invalid-args}", FORMATTER_ARGS_INVALID);
  }

  private void assertErrors(String template, ErrorType... expected) throws CodeSyntaxException {
    List<ErrorInfo> errors = validate(template);
    List<ErrorType> actual = errorTypes(errors);
    if (VERBOSE) {
      for (ErrorInfo error : errors) {
        System.out.println(error.getMessage());
      }
      System.out.println(actual);
    }
    assertEquals(actual.toArray(), expected);
  }

  private List<ErrorInfo> validate(String template) throws CodeSyntaxException {
    Tokenizer tokenizer = tokenizer(template);
    tokenizer.setValidate();
    tokenizer.consume();
    return tokenizer.getErrors();
  }

}
