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

import static com.squarespace.template.InstructionType.IF;
import static com.squarespace.template.InstructionType.OR_PREDICATE;
import static com.squarespace.template.InstructionType.PREDICATE;
import static com.squarespace.template.InstructionType.TEXT;
import static com.squarespace.template.InstructionType.VARIABLE;
import static com.squarespace.template.UnitTestPredicates.EXECUTE_ERROR;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.template.UnitTestFormatters.InvalidArgsFormatter;


@Test(groups = { "unit" })
public class CodeStatsTest extends UnitTestBase {

  private static final Formatter INVALID_ARGS = new InvalidArgsFormatter();

  @Test
  public void testBasics() throws CodeSyntaxException, ArgumentsException {
    CodeMaker mk = maker();
    CodeStats stats = new CodeStats();
    stats.accept(mk.text("foo"));
    stats.accept(mk.predicate(EXECUTE_ERROR));
    stats.accept(mk.or(EXECUTE_ERROR));
    stats.accept(mk.var("a", INVALID_ARGS));
    stats.accept(mk.ifpred(EXECUTE_ERROR));
    stats.accept(mk.var("b", INVALID_ARGS));

    assertEquals(stats.getTotalInstructions(), 6);
    assertEquals((int)stats.getFormatterCounts().get("invalid-args"), 2);
    assertEquals((int)stats.getPredicateCounts().get("execute-error?"), 3);

    assertEquals((int)stats.getInstructionCounts().get(IF), 1);
    assertEquals((int)stats.getInstructionCounts().get(VARIABLE), 2);
    assertEquals((int)stats.getInstructionCounts().get(TEXT), 1);
    assertEquals((int)stats.getInstructionCounts().get(PREDICATE), 1);
    assertEquals((int)stats.getInstructionCounts().get(OR_PREDICATE), 1);
  }
}
