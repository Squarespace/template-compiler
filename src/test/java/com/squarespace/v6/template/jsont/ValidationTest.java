package com.squarespace.template;

import static com.squarespace.template.SyntaxErrorType.DEAD_CODE_BLOCK;
import static com.squarespace.template.SyntaxErrorType.EOF_IN_BLOCK;
import static com.squarespace.template.SyntaxErrorType.EOF_IN_COMMENT;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_ARGS_INVALID;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_UNKNOWN;
import static com.squarespace.template.SyntaxErrorType.INVALID_INSTRUCTION;
import static com.squarespace.template.SyntaxErrorType.MISMATCHED_END;
import static com.squarespace.template.SyntaxErrorType.NOT_ALLOWED_AT_ROOT;
import static com.squarespace.template.SyntaxErrorType.PREDICATE_UNKNOWN;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;


@Test( groups={ "unit" })
public class ValidationTest extends UnitTestBase {

  private static final boolean VERBOSE = false;
  
  /**
   * In validation mode, when encountering a syntax error, the Tokenizer and CodeMachine
   * will both continue processing input, reporting all errors found.
   * 
   * Note: due to the way validate() is currently implemented, the Tokenizer runs first,
   * processing all input into a sequence of instructions.  Then that instruction list is
   * fed to the CodeMachine.  For this reason, the CodeMachine-emitted errors will all
   * appear in the error list after all Tokenizer errors.
   */
  @Test
  public void testRecovery() throws CodeSyntaxException {
    assertErrors("{.sekshun a}{.plooral?}{.singular?}", INVALID_INSTRUCTION, PREDICATE_UNKNOWN, EOF_IN_BLOCK);
    assertErrors("{a|foo-bar}{.alternates with}", FORMATTER_UNKNOWN, NOT_ALLOWED_AT_ROOT);
    assertErrors("{b|invalid-args}{.plural?}{.or}{.or}", FORMATTER_ARGS_INVALID, DEAD_CODE_BLOCK, EOF_IN_BLOCK);
    assertErrors("{.sekshun a}{.end}", INVALID_INSTRUCTION, MISMATCHED_END);
    assertErrors("{.or}{## foo #", EOF_IN_COMMENT, NOT_ALLOWED_AT_ROOT);
  }
  
  private void assertErrors(String template, ErrorType ... expected) throws CodeSyntaxException {
    List<ErrorInfo> errors = validate(template);
    List<ErrorType> actual = errorTypes(errors);
    if (VERBOSE) {
      System.out.println(actual);
    }
    assertEquals(actual.toArray(), expected);
  }
  
  private List<ErrorInfo> validate(String template) throws CodeSyntaxException {
    return compiler().validate(template).errors();
  }
}
