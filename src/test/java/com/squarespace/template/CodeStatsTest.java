package com.squarespace.template;

import static com.squarespace.template.InstructionType.FORMATTER;
import static com.squarespace.template.InstructionType.IF;
import static com.squarespace.template.InstructionType.OR_PREDICATE;
import static com.squarespace.template.InstructionType.PREDICATE;
import static com.squarespace.template.InstructionType.TEXT;
import static com.squarespace.template.UnitTestFormatters.INVALID_ARGS;
import static com.squarespace.template.UnitTestPredicates.EXECUTE_ERROR;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;


@Test( groups={ "unit" })
public class CodeStatsTest extends UnitTestBase {

  @Test
  public void testBasics() throws CodeSyntaxException {
    CodeMaker mk = maker();
    CodeStats stats = new CodeStats();
    stats.accept(mk.text("foo"), mk.predicate(EXECUTE_ERROR), mk.or(EXECUTE_ERROR), mk.formatter("a", INVALID_ARGS));
    stats.accept(mk.ifpred(EXECUTE_ERROR), mk.formatter("b", INVALID_ARGS));
    assertEquals(stats.getTotalInstructions(), 6);
    assertEquals((int)stats.getFormatterCounts().get("invalid-args"), 2);
    assertEquals((int)stats.getPredicateCounts().get("execute-error?"), 3);
    
    assertEquals((int)stats.getInstructionCounts().get(IF), 1);
    assertEquals((int)stats.getInstructionCounts().get(FORMATTER), 2);
    assertEquals((int)stats.getInstructionCounts().get(TEXT), 1);
    assertEquals((int)stats.getInstructionCounts().get(PREDICATE), 1);
    assertEquals((int)stats.getInstructionCounts().get(OR_PREDICATE), 1);
  }
}
