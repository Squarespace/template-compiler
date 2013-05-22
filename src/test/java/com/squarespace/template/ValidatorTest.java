package com.squarespace.template;

import java.util.List;

import org.testng.annotations.Test;


public class ValidatorTest extends UnitTestBase {

  @Test
  public void testValidator() throws CodeException {
    String template = "{.foo}{.bar}{.or}{.end}";
    ValidatedTemplate result = compiler().validate(template);
    List<ErrorInfo> errors = result.getErrors();
    for (ErrorInfo error : errors) {
      System.out.println(error.getMessage());
      System.out.println(error.toJson());
    }
    
    CodeList code = result.getCode();
    for (Instruction inst : code.getInstructions()) {
      System.out.println(inst);
    }

    try {
      CodeMachine machine = machine();
      for (Instruction inst : code.getInstructions()) {
        machine.accept(inst);
      }
    } catch (CodeSyntaxException e) {
      System.out.println(e.getErrorInfo().toJson());
    }
  }
  
}
